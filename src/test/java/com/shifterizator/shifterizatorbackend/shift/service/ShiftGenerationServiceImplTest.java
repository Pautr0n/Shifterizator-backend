package com.shifterizator.shifterizatorbackend.shift.service;

import com.shifterizator.shifterizatorbackend.blackoutdays.model.BlackoutDay;
import com.shifterizator.shifterizatorbackend.blackoutdays.service.BlackoutDayService;
import com.shifterizator.shifterizatorbackend.company.exception.LocationNotFoundException;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours;
import com.shifterizator.shifterizatorbackend.openinghours.service.SpecialOpeningHoursService;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftInstanceRepository;
import com.shifterizator.shifterizatorbackend.shift.repository.ShiftTemplateRepository;
import com.shifterizator.shifterizatorbackend.shift.service.domain.ShiftInstanceDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShiftGenerationServiceImplTest {

    @Mock
    private ShiftInstanceDomainService shiftInstanceDomainService;
    @Mock
    private ShiftTemplateRepository shiftTemplateRepository;
    @Mock
    private ShiftInstanceRepository shiftInstanceRepository;
    @Mock
    private BlackoutDayService blackoutDayService;
    @Mock
    private SpecialOpeningHoursService specialOpeningHoursService;

    @InjectMocks
    private ShiftGenerationServiceImpl service;

    private static Location location(long id) {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company.setId(1L);
        return Location.builder().id(id).name("HQ").address("Main").company(company).build();
    }

    private static Location locationWithOpenDays(long id, Set<DayOfWeek> openDays) {
        Location loc = location(id);
        loc.setOpenDaysOfWeek(openDays);
        return loc;
    }

    private static ShiftTemplate template(long id, Location location, LocalTime start, LocalTime end) {
        ShiftTemplate t = ShiftTemplate.builder()
                .id(id)
                .location(location)
                .startTime(start)
                .endTime(end)
                .requiredEmployees(2)
                .isActive(true)
                .build();
        return t;
    }

    @Test
    void generateMonth_shouldThrowWhenLocationNotFound() {
        when(shiftInstanceDomainService.resolveLocation(999L))
                .thenThrow(new LocationNotFoundException("Location not found"));

        assertThatThrownBy(() -> service.generateMonth(999L, YearMonth.of(2025, 1)))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessageContaining("Location not found");

        verify(blackoutDayService, never()).findByLocationAndMonth(any(), any());
        verify(shiftInstanceRepository, never()).save(any());
    }

    @Test
    void generateMonth_shouldCreateOneInstancePerTemplatePerDayWhenNoBlackoutNoSpecial() {
        Long locationId = 1L;
        Location loc = location(locationId);
        ShiftTemplate t1 = template(1L, loc, LocalTime.of(9, 0), LocalTime.of(17, 0));
        ShiftTemplate t2 = template(2L, loc, LocalTime.of(12, 0), LocalTime.of(20, 0));
        YearMonth yearMonth = YearMonth.of(2025, 1); // 31 days

        when(shiftInstanceDomainService.resolveLocation(locationId)).thenReturn(loc);
        when(blackoutDayService.findByLocationAndMonth(locationId, yearMonth)).thenReturn(List.of());
        when(specialOpeningHoursService.findByLocationAndMonth(locationId, yearMonth)).thenReturn(List.of());
        when(shiftTemplateRepository.findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByPriorityAscStartTimeAsc(locationId))
                .thenReturn(List.of(t1, t2));
        when(shiftInstanceRepository.softDeleteByLocationAndDate(any(), any(), any())).thenReturn(0);
        when(shiftInstanceRepository.save(any(ShiftInstance.class))).thenAnswer(inv -> {
            ShiftInstance i = inv.getArgument(0);
            if (i.getId() == null) {
                ShiftInstance copy = ShiftInstance.builder()
                        .id(100L + inv.getArgument(0).hashCode() % 10000)
                        .shiftTemplate(i.getShiftTemplate())
                        .location(i.getLocation())
                        .date(i.getDate())
                        .startTime(i.getStartTime())
                        .endTime(i.getEndTime())
                        .requiredEmployees(i.getRequiredEmployees())
                        .build();
                return copy;
            }
            return i;
        });

        List<ShiftInstance> result = service.generateMonth(locationId, yearMonth);

        int expectedDays = 31;
        int expectedPerDay = 2; // 2 templates
        assertThat(result).hasSize(expectedDays * expectedPerDay);
        verify(shiftInstanceRepository, times(expectedDays)).softDeleteByLocationAndDate(eq(locationId), any(LocalDate.class), any());
        verify(shiftInstanceRepository, times(expectedDays * expectedPerDay)).save(any(ShiftInstance.class));
    }

    @Test
    void generateMonth_shouldSkipBlackoutDates() {
        Long locationId = 1L;
        Location loc = location(locationId);
        ShiftTemplate t1 = template(1L, loc, LocalTime.of(9, 0), LocalTime.of(17, 0));
        YearMonth yearMonth = YearMonth.of(2025, 1);
        LocalDate blackoutDate = yearMonth.atDay(15);
        BlackoutDay blackout = BlackoutDay.builder().id(1L).location(loc).date(blackoutDate).reason("Holiday").build();

        when(shiftInstanceDomainService.resolveLocation(locationId)).thenReturn(loc);
        when(blackoutDayService.findByLocationAndMonth(locationId, yearMonth)).thenReturn(List.of(blackout));
        when(specialOpeningHoursService.findByLocationAndMonth(locationId, yearMonth)).thenReturn(List.of());
        when(shiftTemplateRepository.findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByPriorityAscStartTimeAsc(locationId))
                .thenReturn(List.of(t1));
        when(shiftInstanceRepository.softDeleteByLocationAndDate(any(), any(), any())).thenReturn(0);
        when(shiftInstanceRepository.save(any(ShiftInstance.class))).thenAnswer(inv -> {
            ShiftInstance i = inv.getArgument(0);
            return ShiftInstance.builder().id(100L).shiftTemplate(i.getShiftTemplate()).location(i.getLocation())
                    .date(i.getDate()).startTime(i.getStartTime()).endTime(i.getEndTime()).requiredEmployees(i.getRequiredEmployees()).build();
        });

        List<ShiftInstance> result = service.generateMonth(locationId, yearMonth);

        // 31 days - 1 blackout = 30 days with 1 instance each
        assertThat(result).hasSize(30);
        assertThat(result).noneMatch(i -> i.getDate().equals(blackoutDate));
        verify(shiftInstanceRepository, times(31)).softDeleteByLocationAndDate(eq(locationId), any(LocalDate.class), any());
    }

    @Test
    void generateMonth_shouldUseSpecialOpeningHoursWhenPresent() {
        Long locationId = 1L;
        Location loc = location(locationId);
        ShiftTemplate t1 = template(1L, loc, LocalTime.of(9, 0), LocalTime.of(17, 0));
        YearMonth yearMonth = YearMonth.of(2025, 1);
        LocalDate specialDate = yearMonth.atDay(10);
        SpecialOpeningHours special = SpecialOpeningHours.builder()
                .id(1L).location(loc).date(specialDate)
                .openTime(LocalTime.of(10, 0)).closeTime(LocalTime.of(18, 0))
                .reason("Event").build();

        when(shiftInstanceDomainService.resolveLocation(locationId)).thenReturn(loc);
        when(blackoutDayService.findByLocationAndMonth(locationId, yearMonth)).thenReturn(List.of());
        when(specialOpeningHoursService.findByLocationAndMonth(locationId, yearMonth)).thenReturn(List.of(special));
        when(shiftTemplateRepository.findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByPriorityAscStartTimeAsc(locationId))
                .thenReturn(List.of(t1));
        when(shiftInstanceRepository.softDeleteByLocationAndDate(any(), any(), any())).thenReturn(0);
        ArgumentCaptor<ShiftInstance> savedCaptor = ArgumentCaptor.forClass(ShiftInstance.class);
        when(shiftInstanceRepository.save(savedCaptor.capture())).thenAnswer(inv -> {
            ShiftInstance i = inv.getArgument(0);
            return ShiftInstance.builder().id(100L).shiftTemplate(i.getShiftTemplate()).location(i.getLocation())
                    .date(i.getDate()).startTime(i.getStartTime()).endTime(i.getEndTime()).requiredEmployees(i.getRequiredEmployees()).build();
        });

        List<ShiftInstance> result = service.generateMonth(locationId, yearMonth);

        assertThat(result).hasSize(31); // 30 normal days (1 per template) + 1 special day (1 instance)
        List<ShiftInstance> saved = savedCaptor.getAllValues();
        ShiftInstance onSpecialDay = saved.stream().filter(i -> i.getDate().equals(specialDate)).findFirst().orElseThrow();
        assertThat(onSpecialDay.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(onSpecialDay.getEndTime()).isEqualTo(LocalTime.of(18, 0));
    }

    @Test
    void generateMonth_shouldSkipSpecialDayWhenNoTemplates() {
        Long locationId = 1L;
        Location loc = location(locationId);
        YearMonth yearMonth = YearMonth.of(2025, 1);
        LocalDate specialDate = yearMonth.atDay(10);
        SpecialOpeningHours special = SpecialOpeningHours.builder()
                .id(1L).location(loc).date(specialDate)
                .openTime(LocalTime.of(10, 0)).closeTime(LocalTime.of(18, 0))
                .reason("Event").build();

        when(shiftInstanceDomainService.resolveLocation(locationId)).thenReturn(loc);
        when(blackoutDayService.findByLocationAndMonth(locationId, yearMonth)).thenReturn(List.of());
        when(specialOpeningHoursService.findByLocationAndMonth(locationId, yearMonth)).thenReturn(List.of(special));
        when(shiftTemplateRepository.findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByPriorityAscStartTimeAsc(locationId))
                .thenReturn(List.of());
        when(shiftInstanceRepository.softDeleteByLocationAndDate(any(), any(), any())).thenReturn(0);

        List<ShiftInstance> result = service.generateMonth(locationId, yearMonth);

        assertThat(result).isEmpty();
        verify(shiftInstanceRepository, never()).save(any());
    }

    @Test
    void generateMonth_shouldSkipClosedWeekdaysWhenLocationHasOpenDays() {
        Long locationId = 1L;
        Set<DayOfWeek> monToSat = EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY);
        Location loc = locationWithOpenDays(locationId, monToSat);
        ShiftTemplate t1 = template(1L, loc, LocalTime.of(9, 0), LocalTime.of(17, 0));
        YearMonth yearMonth = YearMonth.of(2025, 1); // January 2025 has 5 Sundays

        when(shiftInstanceDomainService.resolveLocation(locationId)).thenReturn(loc);
        when(blackoutDayService.findByLocationAndMonth(locationId, yearMonth)).thenReturn(List.of());
        when(specialOpeningHoursService.findByLocationAndMonth(locationId, yearMonth)).thenReturn(List.of());
        when(shiftTemplateRepository.findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByPriorityAscStartTimeAsc(locationId))
                .thenReturn(List.of(t1));
        when(shiftInstanceRepository.softDeleteByLocationAndDate(any(), any(), any())).thenReturn(0);
        when(shiftInstanceRepository.save(any(ShiftInstance.class))).thenAnswer(inv -> {
            ShiftInstance i = inv.getArgument(0);
            return ShiftInstance.builder().id(100L).shiftTemplate(i.getShiftTemplate()).location(i.getLocation())
                    .date(i.getDate()).startTime(i.getStartTime()).endTime(i.getEndTime()).requiredEmployees(i.getRequiredEmployees()).build();
        });

        List<ShiftInstance> result = service.generateMonth(locationId, yearMonth);

        // January 2025: 31 days - 4 Sundays = 27 open days (Mon-Sat), 1 instance each
        assertThat(result).hasSize(27);
        assertThat(result).noneMatch(i -> i.getDate().getDayOfWeek() == DayOfWeek.SUNDAY);
    }

    @Test
    void generateMonth_shouldCreateInstanceOnClosedWeekdayWhenSpecialOpeningHours() {
        Long locationId = 1L;
        Set<DayOfWeek> monToSat = EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY);
        Location loc = locationWithOpenDays(locationId, monToSat);
        ShiftTemplate t1 = template(1L, loc, LocalTime.of(9, 0), LocalTime.of(17, 0));
        YearMonth yearMonth = YearMonth.of(2025, 1);
        LocalDate sunday = yearMonth.atDay(5); // Jan 5, 2025 is Sunday
        SpecialOpeningHours special = SpecialOpeningHours.builder()
                .id(1L).location(loc).date(sunday)
                .openTime(LocalTime.of(10, 0)).closeTime(LocalTime.of(18, 0))
                .reason("Special event").build();

        when(shiftInstanceDomainService.resolveLocation(locationId)).thenReturn(loc);
        when(blackoutDayService.findByLocationAndMonth(locationId, yearMonth)).thenReturn(List.of());
        when(specialOpeningHoursService.findByLocationAndMonth(locationId, yearMonth)).thenReturn(List.of(special));
        when(shiftTemplateRepository.findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByPriorityAscStartTimeAsc(locationId))
                .thenReturn(List.of(t1));
        when(shiftInstanceRepository.softDeleteByLocationAndDate(any(), any(), any())).thenReturn(0);
        when(shiftInstanceRepository.save(any(ShiftInstance.class))).thenAnswer(inv -> {
            ShiftInstance i = inv.getArgument(0);
            return ShiftInstance.builder().id(100L).shiftTemplate(i.getShiftTemplate()).location(i.getLocation())
                    .date(i.getDate()).startTime(i.getStartTime()).endTime(i.getEndTime()).requiredEmployees(i.getRequiredEmployees()).build();
        });

        List<ShiftInstance> result = service.generateMonth(locationId, yearMonth);

        // 27 normal open days (Mon-Sat in Jan 2025) + 1 special Sunday = 28 instances
        assertThat(result).hasSize(28);
        ShiftInstance onSunday = result.stream().filter(i -> i.getDate().equals(sunday)).findFirst().orElseThrow();
        assertThat(onSunday.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(onSunday.getEndTime()).isEqualTo(LocalTime.of(18, 0));
    }
}
