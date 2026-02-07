package com.shifterizator.shifterizatorbackend.company.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
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
    private Boolean isActive = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    public Company(String name,
                   String legalName,
                   String taxId,
                   String email,
                   String phone) {

        this.name = name;
        this.legalName = legalName;
        this.taxId = taxId;
        this.email = email;
        this.phone = phone;
    }

}
