package com.shifterizator.shifterizatorbackend.openinghours.spec;

import com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.YearMonth;

public final class SpecialOpeningHoursSpecs {

    private SpecialOpeningHoursSpecs() {}

    public static Specification<SpecialOpeningHours> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<SpecialOpeningHours> byLocation(Long locationId) {
        return (root, query, cb) -> cb.equal(root.get("location").get("id"), locationId);
    }

    public static Specification<SpecialOpeningHours> byCompany(Long companyId) {
        return (root, query, cb) -> {
            var locationJoin = root.join("location");
            return cb.equal(locationJoin.get("company").get("id"), companyId);
        };
    }

    public static Specification<SpecialOpeningHours> inMonth(YearMonth yearMonth) {
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("date"), start),
                cb.lessThanOrEqualTo(root.get("date"), end)
        );
    }
}
