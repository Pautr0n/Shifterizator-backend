package com.shifterizator.shifterizatorbackend.blackoutdays.spec;

import com.shifterizator.shifterizatorbackend.blackoutdays.model.BlackoutDay;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.YearMonth;

public final class BlackoutDaySpecs {

    private BlackoutDaySpecs() {}

    public static Specification<BlackoutDay> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<BlackoutDay> byLocation(Long locationId) {
        return (root, query, cb) -> cb.equal(root.get("location").get("id"), locationId);
    }

    public static Specification<BlackoutDay> byCompany(Long companyId) {
        return (root, query, cb) -> {
            var locationJoin = root.join("location");
            return cb.equal(locationJoin.get("company").get("id"), companyId);
        };
    }

    public static Specification<BlackoutDay> inMonth(YearMonth yearMonth) {
        var start = yearMonth.atDay(1);
        var end = yearMonth.atEndOfMonth();
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("date"), start),
                cb.lessThanOrEqualTo(root.get("date"), end)
        );
    }

    public static Specification<BlackoutDay> inDateRange(LocalDate start, LocalDate end) {
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("date"), start),
                cb.lessThanOrEqualTo(root.get("date"), end)
        );
    }
}
