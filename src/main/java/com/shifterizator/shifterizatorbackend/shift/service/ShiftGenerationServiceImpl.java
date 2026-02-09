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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Location location = shiftInstanceDomainService.resolveLocation(locationId);

        List<BlackoutDay> blackoutDays = blackoutDayService.findByLocationAndMonth(locationId, yearMonth);
        Set<LocalDate> blackoutDates = blackoutDays.stream().map(BlackoutDay::getDate).collect(Collectors.toSet());

        List<SpecialOpeningHours> specialHoursList = specialOpeningHoursService.findByLocationAndMonth(locationId, yearMonth);
        Map<LocalDate, SpecialOpeningHours> specialHoursByDate = specialHoursList.stream()
                .collect(Collectors.toMap(SpecialOpeningHours::getDate, soh -> soh, (a, b) -> a));

        List<ShiftTemplate> templates = shiftTemplateRepository.findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByStartTimeAsc(locationId);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();
        List<ShiftInstance> created = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            shiftInstanceRepository.softDeleteByLocationAndDate(locationId, date, now);

            if (blackoutDates.contains(date)) {
                continue;
            }

            SpecialOpeningHours special = specialHoursByDate.get(date);
            if (special != null) {
                if (templates.isEmpty()) {
                    continue;
                }
                ShiftTemplate template = templates.get(0);
                ShiftInstance instance = ShiftInstance.builder()
                        .shiftTemplate(template)
                        .location(location)
                        .date(date)
                        .startTime(special.getOpenTime())
                        .endTime(special.getCloseTime())
                        .requiredEmployees(template.getRequiredEmployees())
                        .isComplete(false)
                        .build();
                created.add(shiftInstanceRepository.save(instance));
            } else {
                for (ShiftTemplate template : templates) {
                    ShiftInstance instance = ShiftInstance.builder()
                            .shiftTemplate(template)
                            .location(location)
                            .date(date)
                            .startTime(template.getStartTime())
                            .endTime(template.getEndTime())
                            .requiredEmployees(template.getRequiredEmployees())
                            .isComplete(false)
                            .build();
                    created.add(shiftInstanceRepository.save(instance));
                }
            }
        }

        return created;
    }
}
