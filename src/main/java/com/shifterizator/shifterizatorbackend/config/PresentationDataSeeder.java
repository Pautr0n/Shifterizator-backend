package com.shifterizator.shifterizatorbackend.config;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import com.shifterizator.shifterizatorbackend.company.model.Location;
import com.shifterizator.shifterizatorbackend.company.repository.CompanyRepository;
import com.shifterizator.shifterizatorbackend.company.repository.LocationRepository;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeCompany;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeLocation;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeRepository;
import com.shifterizator.shifterizatorbackend.employee.repository.PositionRepository;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import com.shifterizator.shifterizatorbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;

@Slf4j
@Component
@RequiredArgsConstructor
public class PresentationDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final LocationRepository locationRepository;
    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    public void run(String... args) {
        String enableDemoData = environment.getProperty("ENABLE_DEMO_DATA", "false");
        if (!Boolean.parseBoolean(enableDemoData)) {
            log.info("Presentation data seeder is disabled (ENABLE_DEMO_DATA=false or not set)");
            return;
        }

        if (userRepository.count() > 0) {
            log.info("Database already contains data. Skipping presentation data seeding.");
            return;
        }

        log.info("Starting presentation data seeding...");

        try {
            User superAdmin = createSuperAdmin();
            Company company1 = createCompany1();
            Company company2 = createCompany2();
            
            User companyAdmin1 = createCompanyAdmin(company1, "admin1", "admin1@retailcorp.com");
            User companyAdmin2 = createCompanyAdmin(company2, "admin2", "admin2@techsolutions.com");
            
            User shiftManager1 = createShiftManager(company1, "manager1", "manager1@retailcorp.com");
            User shiftManager2 = createShiftManager(company2, "manager2", "manager2@techsolutions.com");

            Position cashier = createPosition(company1, "Cashier");
            Position stockClerk = createPosition(company1, "Stock Clerk");
            Position developer = createPosition(company2, "Software Developer");
            Position qaEngineer = createPosition(company2, "QA Engineer");

            Location location1 = createLocation(company1, "Downtown Store", "123 Main St, Downtown");
            Location location2 = createLocation(company1, "Mall Branch", "456 Mall Ave, Shopping Center");
            Location location3 = createLocation(company2, "Headquarters", "789 Tech Blvd, Business District");
            Location location4 = createLocation(company2, "Remote Office", "321 Remote St, Suburb");

            Employee emp1 = createEmployee("John", "Doe", "john.doe@retailcorp.com", "555-0101", DayOfWeek.SUNDAY, cashier);
            Employee emp2 = createEmployee("Jane", "Smith", "jane.smith@retailcorp.com", "555-0102", DayOfWeek.MONDAY, cashier);
            Employee emp3 = createEmployee("Bob", "Johnson", "bob.johnson@retailcorp.com", "555-0103", DayOfWeek.SATURDAY, stockClerk);
            Employee emp4 = createEmployee("Alice", "Williams", "alice.williams@techsolutions.com", "555-0201", DayOfWeek.FRIDAY, developer);
            Employee emp5 = createEmployee("Charlie", "Brown", "charlie.brown@techsolutions.com", "555-0202", DayOfWeek.SUNDAY, developer);
            Employee emp6 = createEmployee("Diana", "Davis", "diana.davis@techsolutions.com", "555-0203", DayOfWeek.WEDNESDAY, qaEngineer);

            emp1.setUser(createEmployeeUser(company1, "employee1", "employee1@retailcorp.com", Role.EMPLOYEE));
            emp4.setUser(createEmployeeUser(company2, "employee2", "employee2@techsolutions.com", Role.EMPLOYEE));
            employeeRepository.save(emp1);
            employeeRepository.save(emp4);

            createEmployeeCompany(emp1, company1);
            createEmployeeCompany(emp2, company1);
            createEmployeeCompany(emp3, company1);
            createEmployeeCompany(emp4, company2);
            createEmployeeCompany(emp5, company2);
            createEmployeeCompany(emp6, company2);

            createEmployeeLocation(emp1, location1);
            createEmployeeLocation(emp1, location2);
            createEmployeeLocation(emp2, location1);
            createEmployeeLocation(emp3, location2);
            createEmployeeLocation(emp4, location3);
            createEmployeeLocation(emp5, location3);
            createEmployeeLocation(emp5, location4);
            createEmployeeLocation(emp6, location4);
            
            log.info("Presentation data seeding completed successfully!");
            log.info("Created:");
            log.info("  - {} users (including superadmin)", userRepository.count());
            log.info("  - {} companies", companyRepository.count());
            log.info("  - {} locations", locationRepository.count());
            log.info("  - {} positions", positionRepository.count());
            log.info("  - {} employees", employeeRepository.count());
            
        } catch (Exception e) {
            log.error("Error seeding presentation data", e);
            throw new RuntimeException("Failed to seed presentation data", e);
        }
    }

    private User createSuperAdmin() {
        User superAdmin = new User(
                "superadmin",
                "superadmin@system.local",
                passwordEncoder.encode("SuperAdmin1!"),
                Role.SUPERADMIN,
                null
        );
        superAdmin.setIsActive(true);
        superAdmin.setIsSystemUser(true);
        return userRepository.save(superAdmin);
    }

    private Company createCompany1() {
        Company company = Company.builder()
                .name("Retail Corp")
                .legalName("Retail Corporation Inc.")
                .taxId("TAX-001-2024")
                .email("contact@retailcorp.com")
                .phone("555-1000")
                .country("USA")
                .isActive(true)
                .build();
        return companyRepository.save(company);
    }

    private Company createCompany2() {
        Company company = Company.builder()
                .name("Tech Solutions")
                .legalName("Tech Solutions LLC")
                .taxId("TAX-002-2024")
                .email("info@techsolutions.com")
                .phone("555-2000")
                .country("USA")
                .isActive(true)
                .build();
        return companyRepository.save(company);
    }

    private User createCompanyAdmin(Company company, String username, String email) {
        User admin = new User(
                username,
                email,
                passwordEncoder.encode("Admin123!"),
                Role.COMPANYADMIN,
                company
        );
        admin.setIsActive(true);
        admin.setIsSystemUser(false);
        return userRepository.save(admin);
    }

    private User createShiftManager(Company company, String username, String email) {
        User manager = new User(
                username,
                email,
                passwordEncoder.encode("Manager123!"),
                Role.SHIFTMANAGER,
                company
        );
        manager.setIsActive(true);
        manager.setIsSystemUser(false);
        return userRepository.save(manager);
    }

    private User createEmployeeUser(Company company, String username, String email, Role role) {
        User user = new User(
                username,
                email,
                passwordEncoder.encode("Employee123!"),
                role,
                company
        );
        user.setIsActive(true);
        user.setIsSystemUser(false);
        return userRepository.save(user);
    }

    private Position createPosition(Company company, String name) {
        Position position = Position.builder()
                .name(name)
                .company(company)
                .build();
        return positionRepository.save(position);
    }

    private Location createLocation(Company company, String name, String address) {
        Location location = Location.builder()
                .name(name)
                .address(address)
                .company(company)
                .build();
        return locationRepository.save(location);
    }

    private Employee createEmployee(String name, String surname, String email, String phone, 
                                   DayOfWeek preferredDayOff, Position position) {
        Employee employee = Employee.builder()
                .name(name)
                .surname(surname)
                .email(email)
                .phone(phone)
                .preferredDayOff(preferredDayOff)
                .position(position)
                .build();
        return employeeRepository.save(employee);
    }

    private void createEmployeeCompany(Employee employee, Company company) {
        Employee freshEmployee = employeeRepository.findById(employee.getId())
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employee.getId()));
        EmployeeCompany employeeCompany = EmployeeCompany.builder()
                .employee(freshEmployee)
                .company(company)
                .build();
        freshEmployee.addCompany(employeeCompany);
        employeeRepository.save(freshEmployee);
    }

    private void createEmployeeLocation(Employee employee, Location location) {
        Employee freshEmployee = employeeRepository.findById(employee.getId())
                .orElseThrow(() -> new RuntimeException("Employee not found: " + employee.getId()));
        EmployeeLocation employeeLocation = EmployeeLocation.builder()
                .employee(freshEmployee)
                .location(location)
                .build();
        freshEmployee.addLocation(employeeLocation);
        employeeRepository.save(freshEmployee);
    }
}
