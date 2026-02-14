package com.shifterizator.shifterizatorbackend.company.model;

import com.shifterizator.shifterizatorbackend.employee.model.EmployeeCompany;
import com.shifterizator.shifterizatorbackend.employee.model.Position;
import com.shifterizator.shifterizatorbackend.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String legalName;

    @Column(nullable = false, unique = true)
    private String taxId;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String country;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Location> locations = new HashSet<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Position> positions = new HashSet<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<EmployeeCompany> employeeCompanies = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    private LocalDateTime deletedAt;

    public Company(String name, String legalName, String taxId, String email, String phone) {
        this(null, name, legalName, taxId, email, phone, null, true,
             new HashSet<>(), new HashSet<>(), new HashSet<>(), new HashSet<>(),
             null, null, null, null, null);
    }

    public void addLocation(Location location) {
        locations.add(location);
        location.setCompany(this);
    }

    public void removeLocation(Location location) {
        locations.remove(location);
        location.setCompany(null);
    }

    public void addPosition(Position position) {
        positions.add(position);
        position.setCompany(this);
    }

    public void removePosition(Position position) {
        positions.remove(position);
        position.setCompany(null);
    }

    public void addUser(User user) {
        users.add(user);
        user.setCompany(this);
    }

    public void removeUser(User user) {
        users.remove(user);
        user.setCompany(null);
    }

    public void addEmployeeCompany(EmployeeCompany employeeCompany) {
        employeeCompanies.add(employeeCompany);
        employeeCompany.setCompany(this);
    }

    public void removeEmployeeCompany(EmployeeCompany employeeCompany) {
        employeeCompanies.remove(employeeCompany);
        employeeCompany.setCompany(null);
    }
}
