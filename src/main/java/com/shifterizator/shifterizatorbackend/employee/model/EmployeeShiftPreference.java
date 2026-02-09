package com.shifterizator.shifterizatorbackend.employee.model;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Represents an employee's preference for a shift template.
 * Optional priorityOrder (1 = first choice, 2 = second, etc.) allows ordering preferred shifts.
 */
@Entity
@Table(
        name = "employee_shift_preferences",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_employee_shift_template",
                        columnNames = {"employee_id", "shift_template_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeShiftPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    /**
     * Priority order: 1 = first choice, 2 = second, etc. Lower number = higher preference.
     */
    @Column(name = "priority_order")
    private Integer priorityOrder;

}
