package com.shifterizator.shifterizatorbackend.shift.spec;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import org.springframework.data.jpa.domain.Specification;

public final class ShiftTemplateSpecs {

    private ShiftTemplateSpecs() {}

    public static Specification<ShiftTemplate> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<ShiftTemplate> byLocation(Long locationId) {
        return (root, query, cb) -> cb.equal(root.get("location").get("id"), locationId);
    }

    public static Specification<ShiftTemplate> byPosition(Long positionId) {
        return (root, query, cb) -> cb.equal(root.get("position").get("id"), positionId);
    }

    public static Specification<ShiftTemplate> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("isActive"));
    }
}
