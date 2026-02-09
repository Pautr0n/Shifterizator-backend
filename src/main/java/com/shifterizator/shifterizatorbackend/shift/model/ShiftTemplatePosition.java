package com.shifterizator.shifterizatorbackend.shift.model;

import com.shifterizator.shifterizatorbackend.employee.model.Position;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(
        name = "shift_template_positions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_shift_template_position",
                        columnNames = {"shift_template_id", "position_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplatePosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer requiredCount;
}
