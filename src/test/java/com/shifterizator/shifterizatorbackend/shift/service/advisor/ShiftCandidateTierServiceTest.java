package com.shifterizator.shifterizatorbackend.shift.service.advisor;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeShiftPreference;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeLanguageRepository;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeLanguage;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplateLanguageRequirement;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplatePosition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShiftCandidateTierServiceTest {

    @Mock
    private EmployeeLanguageRepository employeeLanguageRepository;

    @InjectMocks
    private ShiftCandidateTierService tierService;

    private static Location location() {
        Company company = new Company("Co", "Co", "12345678T", "c@c.com", "+34");
        company.setId(1L);
        return Location.builder().id(1L).name("HQ").company(company).build();
    }

    private static ShiftInstance shiftInstance(ShiftTemplate template, LocalDate date) {
        return ShiftInstance.builder()
                .id(10L)
                .shiftTemplate(template)
                .location(template.getLocation())
                .date(date)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build();
    }

    @Test
    void getTier_shouldReturn1WhenAllCriteriaMet() {
        Location loc = location();
        Position position = Position.builder().id(1L).name("Cashier").company(loc.getCompany()).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(loc).build();
        ShiftTemplatePosition stp = ShiftTemplatePosition.builder().shiftTemplate(template).position(position).requiredCount(1).build();
        template.setRequiredPositions(Set.of(stp));
        template.setRequiredLanguageRequirements(Set.of());
        Employee employee = Employee.builder()
                .id(100L)
                .position(position)
                .preferredDayOff(DayOfWeek.SUNDAY)
                .build();
        EmployeeShiftPreference pref = EmployeeShiftPreference.builder().employee(employee).shiftTemplate(template).build();
        employee.setShiftPreferences(Set.of(pref));

        LocalDate wed = LocalDate.of(2025, 1, 8);
        ShiftInstance instance = shiftInstance(template, wed);

        int tier = tierService.getTier(employee, instance, wed);
        assertThat(tier).isEqualTo(1);
    }

    @Test
    void getTier_shouldReturn5WhenPreferredDayOffMatches() {
        Location loc = location();
        Position position = Position.builder().id(1L).name("Cashier").company(loc.getCompany()).build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(loc).build();
        template.setRequiredPositions(Set.of());
        template.setRequiredLanguageRequirements(Set.of());
        Employee employee = Employee.builder()
                .id(100L)
                .position(position)
                .preferredDayOff(DayOfWeek.WEDNESDAY)
                .build();
        employee.setShiftPreferences(Set.of());

        LocalDate wed = LocalDate.of(2025, 1, 8);
        ShiftInstance instance = shiftInstance(template, wed);

        int tier = tierService.getTier(employee, instance, wed);
        assertThat(tier).isEqualTo(5);
    }

    @Test
    void getTier_shouldReturnLowerTierWhenEmployeeSpeaksOneRequiredLanguage() {
        Location loc = location();
        Position position = Position.builder().id(1L).name("Cashier").company(loc.getCompany()).build();
        Language english = Language.builder().id(1L).name("English").code("EN").build();
        ShiftTemplate template = ShiftTemplate.builder().id(1L).location(loc).build();
        ShiftTemplatePosition stp = ShiftTemplatePosition.builder().shiftTemplate(template).position(position).requiredCount(1).build();
        template.setRequiredPositions(Set.of(stp));
        ShiftTemplateLanguageRequirement langReq = ShiftTemplateLanguageRequirement.builder()
                .shiftTemplate(template)
                .language(english)
                .requiredCount(1)
                .build();
        template.setRequiredLanguageRequirements(Set.of(langReq));
        Employee employee = Employee.builder()
                .id(100L)
                .position(position)
                .preferredDayOff(DayOfWeek.SUNDAY)
                .build();
        employee.setShiftPreferences(Set.of());

        LocalDate wed = LocalDate.of(2025, 1, 8);
        ShiftInstance instance = shiftInstance(template, wed);

        when(employeeLanguageRepository.findByEmployee_Id(100L))
                .thenReturn(List.of(EmployeeLanguage.builder().employee(employee).language(english).build()));

        int tier = tierService.getTier(employee, instance, wed);
        assertThat(tier).isEqualTo(3);
    }
}
