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
import lombok.RequiredArgsConstructor;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftGenerationServiceImpl implements ShiftGenerationService {

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

    private GenerationContext loadContext(Long locationId, YearMonth yearMonth) {
        Location location = shiftInstanceDomainService.resolveLocation(locationId);

        List<BlackoutDay> blackoutDays = blackoutDayService.findByLocationAndMonth(locationId, yearMonth);
        Set<LocalDate> blackoutDates = blackoutDays.stream().map(BlackoutDay::getDate).collect(Collectors.toSet());

        List<SpecialOpeningHours> specialHoursList = specialOpeningHoursService.findByLocationAndMonth(locationId, yearMonth);
        Map<LocalDate, SpecialOpeningHours> specialByDate = specialHoursList.stream()
                .collect(Collectors.toMap(SpecialOpeningHours::getDate, soh -> soh, (a, b) -> a));

        List<ShiftTemplate> templates = shiftTemplateRepository
                .findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByStartTimeAsc(locationId);
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
        ShiftInstance instance = ShiftInstance.builder()
                .shiftTemplate(template)
                .location(ctx.location())
                .date(date)
                .startTime(special.getOpenTime())
                .endTime(special.getCloseTime())
                .requiredEmployees(template.getRequiredEmployees())
                .idealEmployees(template.getIdealEmployees())
                .isComplete(false)
                .build();
        created.add(shiftInstanceRepository.save(instance));
    }

    private void createInstancesForNormalDay(GenerationContext ctx, LocalDate date, List<ShiftInstance> created) {
        for (ShiftTemplate template : ctx.templates()) {
            ShiftInstance instance = ShiftInstance.builder()
                    .shiftTemplate(template)
                    .location(ctx.location())
                    .date(date)
                    .startTime(template.getStartTime())
                    .endTime(template.getEndTime())
                    .requiredEmployees(template.getRequiredEmployees())
                    .idealEmployees(template.getIdealEmployees())
                    .isComplete(false)
                    .build();
            created.add(shiftInstanceRepository.save(instance));
        }
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
