package com.shifterizator.shifterizatorbackend.company.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(
        name = "locations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_location_name_company",
                        columnNames = {"name", "company_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Location name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Address is required")
    @Column(nullable = false)
    private String address;

    @NotNull(message = "Company is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "location_open_days", joinColumns = @JoinColumn(name = "location_id"))
    @Column(name = "day_of_week", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> openDaysOfWeek;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;



}
