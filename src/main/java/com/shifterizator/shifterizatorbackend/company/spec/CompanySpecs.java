package com.shifterizator.shifterizatorbackend.company.spec;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import org.springframework.data.jpa.domain.Specification;

public final class CompanySpecs {

    private CompanySpecs() {}

    public static Specification<Company> nameContains(String text) {
        if (text == null || text.isBlank()) {
            return Specification.where((Specification<Company>) null);
        }
        String pattern = "%" + text.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern);
    }

    public static Specification<Company> byCountry(String country) {
        if (country == null || country.isBlank()) {
            return Specification.where((Specification<Company>) null);
        }
        return (root, query, cb) -> cb.equal(cb.lower(root.get("country")), country.toLowerCase());
    }

    public static Specification<Company> byEmail(String email) {
        if (email == null || email.isBlank()) {
            return Specification.where((Specification<Company>) null);
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    public static Specification<Company> byTaxId(String taxId) {
        if (taxId == null || taxId.isBlank()) {
            return Specification.where((Specification<Company>) null);
        }
        return (root, query, cb) -> cb.like(cb.lower(root.get("taxId")), "%" + taxId.toLowerCase() + "%");
    }

    public static Specification<Company> byIsActive(Boolean isActive) {
        if (isActive == null) {
            return Specification.where((Specification<Company>) null);
        }
        return (root, query, cb) -> cb.equal(root.get("isActive"), isActive);
    }
}
