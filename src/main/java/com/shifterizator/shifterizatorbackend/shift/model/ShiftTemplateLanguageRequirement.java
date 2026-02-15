package com.shifterizator.shifterizatorbackend.shift.model;

import com.shifterizator.shifterizatorbackend.language.model.Language;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
        name = "shift_template_language_requirements",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_shift_template_language",
                        columnNames = {"shift_template_id", "language_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftTemplateLanguageRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_template_id", nullable = false)
    private ShiftTemplate shiftTemplate;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    @NotNull
    @Min(0)
    @Column(name = "required_count", nullable = false)
    private Integer requiredCount;
}
