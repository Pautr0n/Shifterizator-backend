package com.shifterizator.shifterizatorbackend.shift.spec;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class ShiftInstanceSpecs {

    private ShiftInstanceSpecs() {}

    public static Specification<ShiftInstance> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<ShiftInstance> byLocation(Long locationId) {
        return (root, query, cb) -> cb.equal(root.get("location").get("id"), locationId);
    }

    public static Specification<ShiftInstance> inDateRange(LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("date"), startDate),
                cb.lessThanOrEqualTo(root.get("date"), endDate)
        );
    }
}
