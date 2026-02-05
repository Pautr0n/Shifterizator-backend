package com.shifterizator.shifterizatorbackend.config;

import com.shifterizator.shifterizatorbackend.auth.service.AuthService;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserSeederTest {

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private AuthService authService;

    @Test
    @DirtiesContext
    void seeder_should_create_initial_users_when_database_empty() {

        assertThat(userRepository.count()).isEqualTo(4);

        Optional<User> superAdmin = userRepository.findByUsername("superadmin");
        assertThat(superAdmin).isPresent();
        assertThat(superAdmin.get().getRole()).isEqualTo(Role.SUPERADMIN);
        assertThat(superAdmin.get().getIsSystemUser()).isTrue();
        assertThat(superAdmin.get().getPassword()).doesNotContain("SuperAdmin1!");
    }

    @Test
    @DirtiesContext
    void seeder_should_not_run_when_users_already_exist() {

        long countAfterFirstRun = userRepository.count();

        long countAfterSecondRun = userRepository.count();

        assertThat(countAfterSecondRun).isEqualTo(countAfterFirstRun);
    }

}