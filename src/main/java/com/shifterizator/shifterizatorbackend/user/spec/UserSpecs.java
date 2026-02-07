package com.shifterizator.shifterizatorbackend.user.spec;

import com.shifterizator.shifterizatorbackend.user.model.User;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecs {

    private UserSpecs() {}

    public static Specification<User> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<User> byRole(String role) {
        if (role == null || role.isBlank()) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.equal(cb.upper(root.get("role")), role.toUpperCase());
    }

    public static Specification<User> byCompany(Long companyId) {
        if (companyId == null) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.equal(root.get("company").get("id"), companyId);
    }

    public static Specification<User> emailContains(String email) {
        if (email == null || email.isBlank()) {
            return Specification.where(null);
        }
        String pattern = "%" + email.toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("email")), pattern);
    }

    public static Specification<User> byIsActive(Boolean isActive) {
        if (isActive == null) {
            return Specification.where(null);
        }
        return (root, query, cb) -> cb.equal(root.get("isActive"), isActive);
    }
}
