package com.shifterizator.shifterizatorbackend.employee.service;

import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeResponseInitializer {

    public void initializeForResponse(Employee employee) {
        if (employee.getUser() != null) {
            employee.getUser().getUsername();
        }
        if (employee.getPosition() != null) {
            employee.getPosition().getName();
        }
        if (employee.getEmployeeCompanies() != null) {
            employee.getEmployeeCompanies().forEach(ec -> ec.getCompany().getId());
        }
        if (employee.getEmployeeLocations() != null) {
            employee.getEmployeeLocations().forEach(el -> el.getLocation().getId());
        }
        if (employee.getEmployeeLanguages() != null) {
            employee.getEmployeeLanguages().forEach(el -> el.getLanguage().getId());
        }
        if (employee.getShiftPreferences() != null) {
            employee.getShiftPreferences().forEach(esp -> esp.getShiftTemplate().getId());
        }
    }
}
