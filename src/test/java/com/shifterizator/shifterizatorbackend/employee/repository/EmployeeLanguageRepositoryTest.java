package com.shifterizator.shifterizatorbackend.employee.repository;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeLanguage;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.language.model.Language;
import com.shifterizator.shifterizatorbackend.language.repository.LanguageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import jakarta.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeLanguageRepositoryTest {

    @Autowired
    private EmployeeLanguageRepository employeeLanguageRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private EntityManager entityManager;

    private Employee employee;
    private Language languageEn;
    private Language languageEs;

    @BeforeEach
    void setUp() {
        Company company = companyRepository.save(new Company(
                "Skynet", "Skynet", "123456789T", "test@test.com", "+34999999999"));
        Position position = positionRepository.save(Position.builder().name("Waiter").company(company).build());
        employee = employeeRepository.save(Employee.builder()
                .name("John")
                .surname("Connor")
                .email("john@example.com")
                .position(position)
                .build());
        languageEn = languageRepository.save(Language.builder().code("EN").name("English").build());
        languageEs = languageRepository.save(Language.builder().code("ES").name("Spanish").build());
    }

    @Test
    void findByEmployeeId_shouldReturnEmployeeLanguages() {
        EmployeeLanguage el1 = EmployeeLanguage.builder().employee(employee).language(languageEn).build();
        EmployeeLanguage el2 = EmployeeLanguage.builder().employee(employee).language(languageEs).build();
        employee.addLanguage(el1);
        employee.addLanguage(el2);
        employeeRepository.save(employee);

        List<EmployeeLanguage> result = employeeLanguageRepository.findByEmployee_Id(employee.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(el -> el.getLanguage().getCode()).containsExactlyInAnyOrder("EN", "ES");
    }

    @Test
    void findByEmployeeId_shouldReturnEmptyWhenNoLanguages() {
        List<EmployeeLanguage> result = employeeLanguageRepository.findByEmployee_Id(employee.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void deleteByEmployeeId_shouldRemoveAllEmployeeLanguages() {
        EmployeeLanguage el1 = EmployeeLanguage.builder().employee(employee).language(languageEn).build();
        employee.addLanguage(el1);
        employeeRepository.save(employee);

        employeeLanguageRepository.deleteByEmployee_Id(employee.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(employeeLanguageRepository.findByEmployee_Id(employee.getId())).isEmpty();
    }
}
