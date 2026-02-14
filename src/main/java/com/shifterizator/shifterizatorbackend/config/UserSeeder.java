package com.shifterizator.shifterizatorbackend.config;

import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!prod")
public class UserSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.count() > 0) {
            return;
        }

        User superAdmin = new User(
                "superadmin",
                "superadmin@system.local",
                passwordEncoder.encode("SuperAdmin1!"),
                Role.SUPERADMIN,
                null
        );
        superAdmin.setIsActive(true);
        superAdmin.setIsSystemUser(true);
        userRepository.save(superAdmin);

        User admin = new User(
                "admin",
                "admin@test.com",
                passwordEncoder.encode("Admin123!"),
                Role.COMPANYADMIN,
                null
        );
        admin.setIsActive(true);
        admin.setIsSystemUser(false);
        userRepository.save(admin);

        User manager = new User(
                "manager",
                "manager@test.com",
                passwordEncoder.encode("Manager123!"),
                Role.SHIFTMANAGER,
                null
        );
        manager.setIsActive(true);
        manager.setIsSystemUser(false);
        userRepository.save(manager);

        User employee = new User(
                "employee",
                "employee@test.com",
                passwordEncoder.encode("Employee123!"),
                Role.EMPLOYEE,
                null
        );
        employee.setIsActive(true);
        employee.setIsSystemUser(false);
        userRepository.save(employee);
    }

}
