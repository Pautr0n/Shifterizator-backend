package com.shifterizator.shifterizatorbackend.availability.repository;

import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import com.shifterizator.shifterizatorbackend.availability.spec.AvailabilitySpecs;
import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.employee.repository.PositionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeAvailabilityRepositoryTest {

    @Autowired
    private EmployeeAvailabilityRepository availabilityRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private CompanyRepository companyRepository;
    @Autowired
    private PositionRepository positionRepository;

    private Employee employee;
    private LocalDate start1;
    private LocalDate end1;

    @BeforeEach
    void setUp() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company = companyRepository.save(company);
        Position position = positionRepository.save(Position.builder().name("Waiter").company(company).build());
        employee = employeeRepository.save(Employee.builder()
                .name("John")
                .surname("Doe")
                .email("john@test.com")
                .position(position)
                .build());
        start1 = LocalDate.now().plusDays(10);
        end1 = LocalDate.now().plusDays(20);
    }

    @Test
    void findByEmployee_IdAndDeletedAtIsNullOrderByStartDateAsc_shouldReturnOnlyNonDeletedOrderedByStart() {
        EmployeeAvailability av1 = EmployeeAvailability.builder().employee(employee).startDate(start1).endDate(end1).type(AvailabilityType.VACATION).build();
        EmployeeAvailability av2 = EmployeeAvailability.builder().employee(employee)
                .startDate(start1.plusDays(30)).endDate(end1.plusDays(30)).type(AvailabilityType.SICK_LEAVE).build();
        availabilityRepository.save(av1);
        availabilityRepository.save(av2);

        List<EmployeeAvailability> result = availabilityRepository.findByEmployee_IdAndDeletedAtIsNullOrderByStartDateAsc(employee.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStartDate()).isBefore(result.get(1).getStartDate());
    }

    @Test
    void findByEmployee_IdAndDeletedAtIsNullOrderByStartDateAsc_shouldExcludeDeleted() {
        EmployeeAvailability av = EmployeeAvailability.builder().employee(employee).startDate(start1).endDate(end1).type(AvailabilityType.VACATION).build();
        av = availabilityRepository.save(av);
        av.setDeletedAt(LocalDateTime.now());
        availabilityRepository.save(av);

        List<EmployeeAvailability> result = availabilityRepository.findByEmployee_IdAndDeletedAtIsNullOrderByStartDateAsc(employee.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findOverlapping_shouldReturnOverlappingRecords() {
        EmployeeAvailability av = EmployeeAvailability.builder().employee(employee).startDate(start1).endDate(end1).type(AvailabilityType.VACATION).build();
        availabilityRepository.save(av);

        List<EmployeeAvailability> overlapping = availabilityRepository.findOverlapping(
                employee.getId(),
                start1.plusDays(2),
                end1.minusDays(2),
                null
        );

        assertThat(overlapping).hasSize(1);
        assertThat(overlapping.get(0).getStartDate()).isEqualTo(start1);
    }

    @Test
    void findOverlapping_shouldExcludeGivenIdWhenUpdating() {
        EmployeeAvailability av1 = EmployeeAvailability.builder().employee(employee).startDate(start1).endDate(end1).type(AvailabilityType.VACATION).build();
        av1 = availabilityRepository.save(av1);

        List<EmployeeAvailability> overlapping = availabilityRepository.findOverlapping(
                employee.getId(),
                start1,
                end1,
                av1.getId()
        );

        assertThat(overlapping).isEmpty();
    }

    @Test
    void findOverlapping_shouldReturnEmptyWhenNoOverlap() {
        EmployeeAvailability av = EmployeeAvailability.builder().employee(employee).startDate(start1).endDate(end1).type(AvailabilityType.VACATION).build();
        availabilityRepository.save(av);

        List<EmployeeAvailability> overlapping = availabilityRepository.findOverlapping(
                employee.getId(),
                end1.plusDays(1),
                end1.plusDays(10),
                null
        );

        assertThat(overlapping).isEmpty();
    }

    @Test
    void findAll_withNotDeletedSpec_shouldExcludeDeleted() {
        EmployeeAvailability av = EmployeeAvailability.builder().employee(employee).startDate(start1).endDate(end1).type(AvailabilityType.VACATION).build();
        availabilityRepository.save(av);
        EmployeeAvailability avDeleted = EmployeeAvailability.builder().employee(employee)
                .startDate(start1.plusDays(30)).endDate(end1.plusDays(30)).type(AvailabilityType.SICK_LEAVE).deletedAt(LocalDateTime.now()).build();
        availabilityRepository.save(avDeleted);

        List<EmployeeAvailability> result = availabilityRepository.findAll(AvailabilitySpecs.notDeleted());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDeletedAt()).isNull();
    }

    @Test
    void findAll_withInDateRangeSpec_shouldReturnOverlappingAvailabilities() {
        EmployeeAvailability av = EmployeeAvailability.builder().employee(employee).startDate(start1).endDate(end1).type(AvailabilityType.VACATION).build();
        availabilityRepository.save(av);

        LocalDate rangeStart = start1.plusDays(1);
        LocalDate rangeEnd = end1.minusDays(1);
        Specification<EmployeeAvailability> spec = AvailabilitySpecs.notDeleted().and(AvailabilitySpecs.inDateRange(rangeStart, rangeEnd));
        List<EmployeeAvailability> result = availabilityRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartDate()).isEqualTo(start1);
        assertThat(result.get(0).getEndDate()).isEqualTo(end1);
    }

    @Test
    void findAll_withByEmployeeAndByTypeSpec_shouldFilterCorrectly() {
        EmployeeAvailability avVacation = EmployeeAvailability.builder().employee(employee).startDate(start1).endDate(end1).type(AvailabilityType.VACATION).build();
        EmployeeAvailability avSick = EmployeeAvailability.builder().employee(employee).startDate(start1.plusDays(30)).endDate(end1.plusDays(30)).type(AvailabilityType.SICK_LEAVE).build();
        availabilityRepository.save(avVacation);
        availabilityRepository.save(avSick);

        Specification<EmployeeAvailability> spec = AvailabilitySpecs.notDeleted()
                .and(AvailabilitySpecs.byEmployee(employee.getId()))
                .and(AvailabilitySpecs.byType(AvailabilityType.VACATION));
        List<EmployeeAvailability> result = availabilityRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(AvailabilityType.VACATION);
    }
}
