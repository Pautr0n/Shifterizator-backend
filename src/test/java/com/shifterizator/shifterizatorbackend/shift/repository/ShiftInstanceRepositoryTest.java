package com.shifterizator.shifterizatorbackend.shift.repository;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplatePosition;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.employee.repository.PositionRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ShiftInstanceRepositoryTest {

    @Autowired
    private ShiftInstanceRepository repository;

    @Autowired
    private ShiftTemplateRepository shiftTemplateRepository;

    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EntityManager entityManager;

    private Location location1;
    private ShiftTemplate template1;

    @BeforeEach
    void setUp() {
        Company company = new Company("Skynet", "Skynet", "12345678T", "test@test.com", "+34999999999");
        company = companyRepository.save(company);

        location1 = locationRepository.save(Location.builder()
                .name("HQ")
                .address("Main")
                .company(company)
                .build());

        Position position1 = positionRepository.save(Position.builder()
                .name("Sales Assistant")
                .company(company)
                .build());

        template1 = ShiftTemplate.builder()
                .location(location1)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .isActive(true)
                .build();
        ShiftTemplatePosition stp = ShiftTemplatePosition.builder()
                .shiftTemplate(template1)
                .position(position1)
                .requiredCount(2)
                .build();
        template1.setRequiredPositions(new HashSet<>(Set.of(stp)));
        shiftTemplateRepository.save(template1);
    }

    @Test
    void findByLocation_IdAndDateAndDeletedAtIsNullOrderByStartTimeAsc_shouldReturnOnlyNonDeletedOrdered() {
        LocalDate date = LocalDate.of(2024, 12, 24);
        ShiftInstance instance1 = ShiftInstance.builder()
                .shiftTemplate(template1)
                .location(location1)
                .date(date)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(18, 0))
                .requiredEmployees(2)
                .build();
        repository.save(instance1);

        ShiftInstance instance2 = ShiftInstance.builder()
                .shiftTemplate(template1)
                .location(location1)
                .date(date)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .requiredEmployees(2)
                .build();
        repository.save(instance2);

        entityManager.flush();
        entityManager.clear();

        List<ShiftInstance> result = repository.findByLocation_IdAndDateAndDeletedAtIsNullOrderByStartTimeAsc(location1.getId(), date);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStartTime()).isBefore(result.get(1).getStartTime());
    }

    @Test
    void countActiveAssignments_shouldReturnCorrectCount() {
        LocalDate date = LocalDate.of(2024, 12, 24);
        ShiftInstance instance = ShiftInstance.builder()
                .shiftTemplate(template1)
                .location(location1)
                .date(date)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .requiredEmployees(2)
                .build();
        repository.save(instance);

        Company company = location1.getCompany();
        Position position = template1.getRequiredPositions().iterator().next().getPosition();
        Employee employee1 = Employee.builder()
                .name("John")
                .surname("Doe")
                .position(position)
                .build();
        employee1 = employeeRepository.save(employee1);

        Employee employee2 = Employee.builder()
                .name("Jane")
                .surname("Smith")
                .position(position)
                .build();
        employee2 = employeeRepository.save(employee2);

        ShiftAssignment assignment1 = ShiftAssignment.builder()
                .shiftInstance(instance)
                .employee(employee1)
                .build();
        shiftAssignmentRepository.save(assignment1);

        ShiftAssignment assignment2 = ShiftAssignment.builder()
                .shiftInstance(instance)
                .employee(employee2)
                .build();
        shiftAssignmentRepository.save(assignment2);

        entityManager.flush();
        entityManager.clear();

        int count = repository.countActiveAssignments(instance.getId());

        assertThat(count).isEqualTo(2);
    }
}
