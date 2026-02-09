package com.shifterizator.shifterizatorbackend.openinghours.model;

import com.shifterizator.shifterizatorbackend.company.model.Location;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "special_opening_hours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialOpeningHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime openTime;

    @Column(nullable = false)
    private LocalTime closeTime;

    @Column(nullable = false, length = 200)
    private String reason;

    @Column(length = 20)
    private String colorCode;

    @Column(nullable = false)
    @Builder.Default
    private Boolean appliesToCompany = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    private LocalDateTime deletedAt;
}
