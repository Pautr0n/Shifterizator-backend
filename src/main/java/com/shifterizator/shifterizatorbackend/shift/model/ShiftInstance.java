package com.shifterizator.shifterizatorbackend.shift.model;

import com.shifterizator.shifterizatorbackend.company.model.Location;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shift_instances")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    @Builder.Default
    private Integer requiredEmployees = 1;

    @Column(name = "ideal_employees")
    private Integer idealEmployees;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isComplete = false;

    @Column(length = 200)
    private String notes;

    @OneToMany(mappedBy = "shiftInstance", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ShiftAssignment> assignments = new HashSet<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String createdBy;
    private String updatedBy;

    private LocalDateTime deletedAt;
}
