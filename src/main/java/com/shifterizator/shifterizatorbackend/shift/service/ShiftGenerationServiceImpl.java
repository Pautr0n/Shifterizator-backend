package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.blackoutdays.model.BlackoutDay;
import com.shifterizator.shifterizatorbackend.blackoutdays.service.BlackoutDayService;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours;
import com.shifterizator.shifterizatorbackend.openinghours.service.SpecialOpeningHoursService;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftInstanceRepository;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftTemplateRepository;
import com.shifterizator.shifterizatorbackend.shift.service.domain.ShiftInstanceDomainService;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftGenerationConflictException;
import com.shifterizator.shifterizatorbackend.shift.exception.ShiftValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftGenerationServiceImpl implements ShiftGenerationService {

    private static final int MAX_RANGE_DAYS = 56;

    private final ShiftInstanceDomainService shiftInstanceDomainService;
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final ShiftInstanceRepository shiftInstanceRepository;
    private final BlackoutDayService blackoutDayService;
    private final SpecialOpeningHoursService specialOpeningHoursService;

    @Override
    public List<ShiftInstance> generateMonth(Long locationId, YearMonth yearMonth) {
        GenerationContext ctx = loadContext(locationId, yearMonth);
        List<ShiftInstance> created = new ArrayList<>();

        for (LocalDate date = ctx.firstDay(); !date.isAfter(ctx.lastDay()); date = date.plusDays(1)) {
            shiftInstanceRepository.softDeleteByLocationAndDate(ctx.locationId(), date, ctx.deletedAt());

            if (ctx.blackoutDates().contains(date)) {
                continue;
            }

            SpecialOpeningHours special = ctx.specialByDate().get(date);

            if (special != null) {
                createInstancesForSpecialDay(ctx, date, special, created);
            } else if(isWeekdayClosedForLocation(ctx.location(),date)){
                continue;
            } else {
                createInstancesForNormalDay(ctx, date, created);
            }
        }

        return created;
    }

    @Override
    public List<ShiftInstance> generateRange(Long locationId, LocalDate startDate, LocalDate endDate, boolean replaceExisting) {
        validateRange(startDate, endDate);
        if (!replaceExisting) {
            List<ShiftInstance> existing = shiftInstanceRepository
                    .findByLocation_IdAndDateBetweenAndDeletedAtIsNullOrderByDateAscStartTimeAsc(locationId, startDate, endDate);
            if (!existing.isEmpty()) {
                List<LocalDate> existingDates = existing.stream()
                        .map(ShiftInstance::getDate)
                        .distinct()
                        .sorted()
                        .toList();
                throw new ShiftGenerationConflictException(
                        "Some dates already have shifts. Set replaceExisting=true to replace them.",
                        existingDates);
            }
        }
        GenerationContext ctx = loadContextForRange(locationId, startDate, endDate);
        List<ShiftInstance> created = new ArrayList<>();

        for (LocalDate date = ctx.firstDay(); !date.isAfter(ctx.lastDay()); date = date.plusDays(1)) {
            shiftInstanceRepository.softDeleteByLocationAndDate(ctx.locationId(), date, ctx.deletedAt());

            if (ctx.blackoutDates().contains(date)) {
                continue;
            }

            SpecialOpeningHours special = ctx.specialByDate().get(date);

            if (special != null) {
                createInstancesForSpecialDay(ctx, date, special, created);
            } else if (isWeekdayClosedForLocation(ctx.location(), date)) {
                continue;
            } else {
                createInstancesForNormalDay(ctx, date, created);
            }
        }

        return created;
    }

    private void validateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new ShiftValidationException("Start date must be a Monday");
        }
        if (endDate.getDayOfWeek() != DayOfWeek.SUNDAY) {
            throw new ShiftValidationException("End date must be a Sunday");
        }
        if (endDate.isBefore(startDate)) {
            throw new ShiftValidationException("End date must be on or after start date");
        }
        long days = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (days > MAX_RANGE_DAYS) {
            throw new ShiftValidationException("Range must not exceed 8 weeks");
        }
    }

    private GenerationContext loadContextForRange(Long locationId, LocalDate startDate, LocalDate endDate) {
        Location location = shiftInstanceDomainService.resolveLocation(locationId);

        List<BlackoutDay> blackoutDays = blackoutDayService.findByLocationAndDateRange(locationId, startDate, endDate);
        Set<LocalDate> blackoutDates = blackoutDays.stream().map(BlackoutDay::getDate).collect(Collectors.toSet());

        List<SpecialOpeningHours> specialHoursList = specialOpeningHoursService.findByLocationAndDateRange(locationId, startDate, endDate);
        Map<LocalDate, SpecialOpeningHours> specialByDate = specialHoursList.stream()
                .collect(Collectors.toMap(SpecialOpeningHours::getDate, soh -> soh, (a, b) -> a));

        List<ShiftTemplate> templates = shiftTemplateRepository
                .findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByPriorityAscStartTimeAsc(locationId);
        LocalDateTime deletedAt = LocalDateTime.now();

        return new GenerationContext(
                locationId,
                location,
                blackoutDates,
                specialByDate,
                templates,
                startDate,
                endDate,
                deletedAt
        );
    }

    private GenerationContext loadContext(Long locationId, YearMonth yearMonth) {
        Location location = shiftInstanceDomainService.resolveLocation(locationId);

        List<BlackoutDay> blackoutDays = blackoutDayService.findByLocationAndMonth(locationId, yearMonth);
        Set<LocalDate> blackoutDates = blackoutDays.stream().map(BlackoutDay::getDate).collect(Collectors.toSet());

        List<SpecialOpeningHours> specialHoursList = specialOpeningHoursService.findByLocationAndMonth(locationId, yearMonth);
        Map<LocalDate, SpecialOpeningHours> specialByDate = specialHoursList.stream()
                .collect(Collectors.toMap(SpecialOpeningHours::getDate, soh -> soh, (a, b) -> a));

        List<ShiftTemplate> templates = shiftTemplateRepository
                .findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByPriorityAscStartTimeAsc(locationId);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();
        LocalDateTime deletedAt = LocalDateTime.now();

        return new GenerationContext(
                locationId,
                location,
                blackoutDates,
                specialByDate,
                templates,
                firstDay,
                lastDay,
                deletedAt
        );
    }

    private boolean isWeekdayClosedForLocation(Location location, LocalDate date) {
        Set<DayOfWeek> openDays = location.getOpenDaysOfWeek();
        if (openDays == null || openDays.isEmpty()) {
            return false;
        }
        return !openDays.contains(date.getDayOfWeek());
    }

    private void createInstancesForSpecialDay(GenerationContext ctx, LocalDate date, SpecialOpeningHours special,
                                              List<ShiftInstance> created) {
        if (ctx.templates().isEmpty()) {
            return;
        }
        ShiftTemplate template = ctx.templates().get(0);
        var requiredAndIdeal = requiredAndIdealFromTemplate(template);
        ShiftInstance instance = ShiftInstance.builder()
                .shiftTemplate(template)
                .location(ctx.location())
                .date(date)
                .startTime(special.getOpenTime())
                .endTime(special.getCloseTime())
                .requiredEmployees(requiredAndIdeal.required())
                .idealEmployees(requiredAndIdeal.ideal())
                .isComplete(false)
                .build();
        created.add(shiftInstanceRepository.save(instance));
    }

    private void createInstancesForNormalDay(GenerationContext ctx, LocalDate date, List<ShiftInstance> created) {
        for (ShiftTemplate template : ctx.templates()) {
            var requiredAndIdeal = requiredAndIdealFromTemplate(template);
            ShiftInstance instance = ShiftInstance.builder()
                    .shiftTemplate(template)
                    .location(ctx.location())
                    .date(date)
                    .startTime(template.getStartTime())
                    .endTime(template.getEndTime())
                    .requiredEmployees(requiredAndIdeal.required())
                    .idealEmployees(requiredAndIdeal.ideal())
                    .isComplete(false)
                    .build();
            created.add(shiftInstanceRepository.save(instance));
        }
    }

    /**
     * Computes required and ideal employee counts from template: from requiredPositions when present and non-empty,
     * otherwise from template's requiredEmployees/idealEmployees (default 1 for required).
     */
    private static record RequiredAndIdeal(int required, Integer ideal) {}

    private static RequiredAndIdeal requiredAndIdealFromTemplate(ShiftTemplate template) {
        if (template.getRequiredPositions() != null && !template.getRequiredPositions().isEmpty()) {
            int required = template.getRequiredPositions().stream()
                    .mapToInt(stp -> stp.getRequiredCount() != null ? stp.getRequiredCount() : 0)
                    .sum();
            int ideal = template.getRequiredPositions().stream()
                    .mapToInt(stp -> stp.getIdealCount() != null ? stp.getIdealCount() : (stp.getRequiredCount() != null ? stp.getRequiredCount() : 0))
                    .sum();
            return new RequiredAndIdeal(required, ideal);
        }
        int required = template.getRequiredEmployees() != null ? template.getRequiredEmployees() : 1;
        Integer ideal = template.getIdealEmployees();
        return new RequiredAndIdeal(required, ideal);
    }

    private record GenerationContext(
            Long locationId,
            Location location,
            Set<LocalDate> blackoutDates,
            Map<LocalDate, SpecialOpeningHours> specialByDate,
            List<ShiftTemplate> templates,
            LocalDate firstDay,
            LocalDate lastDay,
            LocalDateTime deletedAt
    ) {
    }
}
