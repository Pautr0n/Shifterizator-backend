package com.shifterizator.shifterizatorbackend.shift.model;

import com.shifterizator.shifterizatorbackend.company.model.Location;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shift_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @OneToMany(
            mappedBy = "shiftTemplate",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<ShiftTemplatePosition> requiredPositions = new HashSet<>();

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    @Builder.Default
    private Integer requiredEmployees = 1;

    /** Target headcount when enough staff available; must be >= requiredEmployees if set. */
    @Column(name = "ideal_employees")
    private Integer idealEmployees;

    @Column(length = 200)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "shift_template_languages",
            joinColumns = @JoinColumn(name = "shift_template_id"),
            inverseJoinColumns = @JoinColumn(name = "language_id")
    )
    @Builder.Default
    private Set<com.shifterizator.shifterizatorbackend.language.model.Language> requiredLanguages = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Scheduler priority: lower value = higher priority (e.g. 1 = afternoon first).
     * Null = lowest priority; used as tie-breaker with start time.
     */
    @Column(name = "priority_order")
    private Integer priority;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    private LocalDateTime deletedAt;
}
