package com.shifterizator.shifterizatorbackend.employee.spec;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.employee.model.*;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.employee.repository.PositionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeSpecsTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Test
    void specs_shouldFilterByCompanyLocationNamePositionAndActive() {
        Company company1 = new Company();
        company1.setId(1L);
        company1.setName("Skynet");

        Company company2 = new Company();
        company2.setId(2L);
        company2.setName("Cyberdyne");

        Location loc1 = locationRepository.save(Location.builder().name("HQ").address("A").company(company1).build());
        Location loc2 = locationRepository.save(Location.builder().name("Branch").address("B").company(company2).build());

        Position waiter1 = positionRepository.save(Position.builder().name("Waiter").company(company1).build());
        Position waiter2 = positionRepository.save(Position.builder().name("Waiter").company(company2).build());
        Position cook = positionRepository.save(Position.builder().name("Cook").company(company1).build());

        Employee e1 = Employee.builder()
                .name("John")
                .surname("Connor")
                .position(waiter1)
                .build();
        Employee e2 = Employee.builder()
                .name("Johnny")
                .surname("Smith")
                .position(waiter2)
                .build();
        Employee e3 = Employee.builder()
                .name("Sarah")
                .surname("Connor")
                .position(cook)
                .deletedAt(LocalDateTime.now())
                .build();

        employeeRepository.saveAll(List.of(e1, e2, e3));

        EmployeeCompany ec1 = EmployeeCompany.builder().employee(e1).company(company1).build();
        EmployeeCompany ec2 = EmployeeCompany.builder().employee(e2).company(company2).build();
        e1.addCompany(ec1);
        e2.addCompany(ec2);

        EmployeeLocation el1 = EmployeeLocation.builder().employee(e1).location(loc1).build();
        EmployeeLocation el2 = EmployeeLocation.builder().employee(e2).location(loc2).build();
        e1.addLocation(el1);
        e2.addLocation(el2);

        employeeRepository.saveAll(List.of(e1, e2));

        Specification<Employee> spec = Specification.where(EmployeeSpecs.onlyActive())
                .and(EmployeeSpecs.byCompany(company1.getId()))
                .and(EmployeeSpecs.byLocation(loc1.getId()))
                .and(EmployeeSpecs.nameContains("john"))
                .and(EmployeeSpecs.byPosition("Waiter"));

        List<Employee> result = employeeRepository.findAll(spec);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("John");
    }
}
