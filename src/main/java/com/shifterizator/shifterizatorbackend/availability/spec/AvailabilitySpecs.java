package com.shifterizator.shifterizatorbackend.availability.spec;

import com.shifterizator.shifterizatorbackend.availability.model.AvailabilityType;
import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class AvailabilitySpecs {

    private AvailabilitySpecs() {}

    public static Specification<EmployeeAvailability> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<EmployeeAvailability> byEmployee(Long employeeId) {
        return (root, query, cb) -> cb.equal(root.get("employee").get("id"), employeeId);
    }

    public static Specification<EmployeeAvailability> byType(AvailabilityType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    /** Range filter: availability overlaps [rangeStart, rangeEnd] (endDate >= rangeStart AND startDate <= rangeEnd). */
    public static Specification<EmployeeAvailability> inDateRange(LocalDate rangeStart, LocalDate rangeEnd) {
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("endDate"), rangeStart),
                cb.lessThanOrEqualTo(root.get("startDate"), rangeEnd)
        );
    }

    /**
     * Filter by location: availabilities of employees who work at the given location.
     */
    public static Specification<EmployeeAvailability> byEmployeeLocation(Long locationId) {
        return (root, query, cb) -> {
            var employeeJoin = root.join("employee");
            var locationsJoin = employeeJoin.join("employeeLocations");
            return cb.equal(locationsJoin.get("location").get("id"), locationId);
        };
    }
}
