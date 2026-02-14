package com.shifterizator.shifterizatorbackend.user.repository;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Autowired
    private CompanyRepository companyRepository;

    private User sampleUser;
    private Optional<User> result;
    private List<User> resultSet;

    @BeforeEach
    void setup() {

        Company company = new Company(
                "Test Company",
                "Test Legal Name",
                "A12345678",
                "company@test.com",
                "+34999999999"
        );

        company = companyRepository.save(company);

        sampleUser = new User(
                "john123",
                "john@mail.com",
                "hashed",
                Role.EMPLOYEE,
                company
        );

        repository.save(sampleUser);
    }

    @Test
    void existsByUsername_should_return_true_when_username_exists() {
        boolean exists = repository.existsByUsername("john123");
        assertTrue(exists);
    }

    @Test
    void existsByUsername_should_return_false_when_username_does_not_exist() {
        boolean exists = repository.existsByUsername("nope");
        assertFalse(exists);
    }

    @Test
    void existsByEmail_should_return_true_when_email_exists() {
        boolean exists = repository.existsByEmail("john@mail.com");
        assertTrue(exists);
    }

    @Test
    void existsByEmail_should_return_false_when_email_does_not_exist() {
        boolean exists = repository.existsByEmail("nope@mail.com");
        assertFalse(exists);
    }

    @Test
    void findByUsernameContainingIgnoreCase_should_return_entities_when_match_exists() {

        User user2 = new User(
                "Johnathan",
                "john2@mail.com",
                "hashed",
                Role.EMPLOYEE,
                null
        );
        repository.save(user2);

        resultSet = repository.findByUsernameContainingIgnoreCase("john");

        assertFalse(resultSet.isEmpty());
        assertEquals(2, resultSet.size());
        assertEquals("john123", resultSet.get(0).getUsername());
        assertEquals("Johnathan", resultSet.get(1).getUsername());
    }

    @Test
    void findByUsernameContainingIgnoreCase_should_return_empty_when_no_match() {

        resultSet = repository.findByUsernameContainingIgnoreCase("zzz");

        assertTrue(resultSet.isEmpty());
    }

    @Test
    void findByIsActive_should_return_active_users() {

        User user2 = new User(
                "activeUser",
                "active@mail.com",
                "hashed",
                Role.EMPLOYEE,
                null
        );
        repository.save(user2);

        resultSet = repository.findByIsActive(true);

        assertFalse(resultSet.isEmpty());
        assertEquals(2, resultSet.size());
    }

    @Test
    void findByIsActive_should_return_empty_when_no_active_users() {

        sampleUser.setIsActive(false);
        repository.save(sampleUser);

        resultSet = repository.findByIsActive(true);

        assertTrue(resultSet.isEmpty());
    }

    @Test
    void findByUsernameContainingIgnoreCaseAndIsActive_should_return_matching_active_users() {

        User user2 = new User(
                "johnny",
                "johnny@mail.com",
                "hashed",
                Role.EMPLOYEE,
                null
        );
        repository.save(user2);

        resultSet = repository.findByUsernameContainingIgnoreCaseAndIsActive("john", true);

        assertFalse(resultSet.isEmpty());
        assertEquals(2, resultSet.size());
    }

    @Test
    void findByUsernameContainingIgnoreCaseAndIsActive_should_return_empty_when_no_match() {

        resultSet = repository.findByUsernameContainingIgnoreCaseAndIsActive("zzz", true);
        assertTrue(resultSet.isEmpty());

        resultSet = repository.findByUsernameContainingIgnoreCaseAndIsActive("john", false);
        assertTrue(resultSet.isEmpty());
    }
}
