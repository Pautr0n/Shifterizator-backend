package com.shifterizator.shifterizatorbackend.employee.model;

import com.shifterizator.shifterizatorbackend.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String surname;

    @Email
    @Column
    private String email;

    @Column
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_day_off", length = 10)
    private DayOfWeek preferredDayOff;

    @Column(name = "shifts_per_week")
    private Integer shiftsPerWeek;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToMany(
            mappedBy = "employee",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<EmployeeCompany> employeeCompanies = new HashSet<>();

    @OneToMany(
            mappedBy = "employee",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<EmployeeLocation> employeeLocations = new HashSet<>();

    @OneToMany(
            mappedBy = "employee",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<EmployeeLanguage> employeeLanguages = new HashSet<>();

    @OneToMany(
            mappedBy = "employee",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<EmployeeShiftPreference> shiftPreferences = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    private LocalDateTime deletedAt;

    public void addCompany(EmployeeCompany employeeCompany) {
        employeeCompanies.add(employeeCompany);
        employeeCompany.setEmployee(this);
    }

    public void removeCompany(EmployeeCompany employeeCompany) {
        employeeCompanies.remove(employeeCompany);
        employeeCompany.setEmployee(null);
    }

    public void addLocation(EmployeeLocation employeeLocation) {
        employeeLocations.add(employeeLocation);
        employeeLocation.setEmployee(this);
    }

    public void removeLocation(EmployeeLocation employeeLocation) {
        employeeLocations.remove(employeeLocation);
        employeeLocation.setEmployee(null);
    }

    public void addLanguage(EmployeeLanguage employeeLanguage) {
        employeeLanguages.add(employeeLanguage);
        employeeLanguage.setEmployee(this);
    }

    public void removeLanguage(EmployeeLanguage employeeLanguage) {
        employeeLanguages.remove(employeeLanguage);
        employeeLanguage.setEmployee(null);
    }

    public void addShiftPreference(EmployeeShiftPreference preference) {
        shiftPreferences.add(preference);
        preference.setEmployee(this);
    }

    public void removeShiftPreference(EmployeeShiftPreference preference) {
        shiftPreferences.remove(preference);
        preference.setEmployee(null);
    }
}

