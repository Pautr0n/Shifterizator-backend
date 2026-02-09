package com.shifterizator.shifterizatorbackend.blackoutdays.model;

import com.shifterizator.shifterizatorbackend.company.model.Location;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blackout_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlackoutDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 200)
    private String reason;

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
