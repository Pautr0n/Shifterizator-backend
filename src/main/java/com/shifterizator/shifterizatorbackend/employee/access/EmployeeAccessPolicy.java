package com.shifterizator.shifterizatorbackend.employee.access;

import com.shifterizator.shifterizatorbackend.employee.dto.EmployeeRequestDto;
import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.user.exception.ForbiddenOperationException;
import com.shifterizator.shifterizatorbackend.user.model.Role;
import com.shifterizator.shifterizatorbackend.user.model.User;
import org.springframework.stereotype.Component;

/**
 * Encapsulates authorization rules for employee access.
 * COMPANYADMIN can only access employees of their own company and create employees for their company only.
 */
@Component
public class EmployeeAccessPolicy {

    /** COMPANYADMIN can only access employees that belong to their company. */
    public void ensureCanAccessEmployee(Employee employee, User currentUser) {
        if (currentUser.getRole() != Role.COMPANYADMIN || currentUser.getCompany() == null) {
            return;
        }
        Long userCompanyId = currentUser.getCompany().getId();
        boolean belongs = employee.getEmployeeCompanies() != null
                && employee.getEmployeeCompanies().stream()
                .anyMatch(ec -> ec.getCompany() != null && userCompanyId.equals(ec.getCompany().getId()));
        if (!belongs) {
            throw new ForbiddenOperationException("You can only view and manage employees of your own company.");
        }
    }

    /** COMPANYADMIN can only create employees for their own company. */
    public void ensureCompanyScopeForCreate(EmployeeRequestDto dto, User currentUser) {
        if (currentUser.getRole() != Role.COMPANYADMIN || currentUser.getCompany() == null) {
            return;
        }
        Long userCompanyId = currentUser.getCompany().getId();
        if (dto.companyIds() == null || dto.companyIds().size() != 1 || !dto.companyIds().contains(userCompanyId)) {
            throw new ForbiddenOperationException("You can only create employees for your own company.");
        }
    }
}
