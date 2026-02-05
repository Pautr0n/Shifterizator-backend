package com.shifterizator.shifterizatorbackend.employee.spec;

import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import org.springframework.data.jpa.domain.Specification;

public class EmployeeSpecs {

    private EmployeeSpecs() {}

    public static Specification<Employee> byCompany(Long companyId) {
        return (root, query, cb) -> {
            var join = root.join("employeeCompanies");
            return cb.equal(join.get("company").get("id"), companyId);
        };
    }

    public static Specification<Employee> byLocation(Long locationId) {
        return (root, query, cb) -> {
            var join = root.join("employeeLocations");
            return cb.equal(join.get("location").get("id"), locationId);
        };
    }

    public static Specification<Employee> nameContains(String text) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + text.toLowerCase() + "%");
    }


    public static Specification<Employee> byPosition(String positionName) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("position").get("name")), positionName.toLowerCase());
    }


    public static Specification<Employee> onlyActive() {
        return (root, query, cb) ->
                cb.isNull(root.get("deletedAt"));
    }
}
