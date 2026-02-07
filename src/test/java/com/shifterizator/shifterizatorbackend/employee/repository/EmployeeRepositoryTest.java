package com.shifterizator.shifterizatorbackend.employee.repository;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.employee.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Test
    void existsByEmailAndCompany_shouldReturnTrueWhenEmailExistsForCompany() {
        Company company = companyRepository.save(new Company(
                "Skynet",
                "Skynet",
                "123456789T",
                "test@test.com",
                "9656598998"));
        Position position = positionRepository.save(Position.builder().name("Waiter").company(company).build());

        Employee employee = Employee.builder()
                .name("John")
                .surname("Connor")
                .email("john@example.com")
                .position(position)
                .build();

        employeeRepository.save(employee);

        EmployeeCompany ec = EmployeeCompany.builder()
                .employee(employee)
                .company(company)
                .build();
        employee.addCompany(ec);
        employeeRepository.save(employee);

        boolean exists = employeeRepository.existsByEmailAndCompany("john@example.com", company.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmailAndCompany_shouldReturnFalseWhenEmailNotExistsForCompany() {
        Company company = companyRepository.save(new Company(
                "Skynet",
                "Skynet",
                "123456789T",
                "test@test.com",
                "9656598998"));

        boolean exists = employeeRepository.existsByEmailAndCompany("john@example.com", company.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void findActiveById_shouldIgnoreSoftDeletedEmployees() {
        Company company = companyRepository.save(new Company(
                "Skynet",
                "Skynet",
                "123456789T",
                "test@test.com",
                "9656598998"));
        Position position = positionRepository.save(Position.builder().name("Waiter").company(company).build());

        Employee active = Employee.builder()
                .name("John")
                .surname("Connor")
                .position(position)
                .build();

        Employee deleted = Employee.builder()
                .name("Sarah")
                .surname("Connor")
                .position(position)
                .deletedAt(LocalDateTime.now())
                .build();

        employeeRepository.save(active);
        employeeRepository.save(deleted);

        assertThat(employeeRepository.findActiveById(active.getId())).isPresent();
        assertThat(employeeRepository.findActiveById(deleted.getId())).isEmpty();
    }

}