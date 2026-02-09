package com.shifterizator.shifterizatorbackend.shift.service.advisor;

import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeShiftPreference;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides non-blocking warnings when an assignment conflicts with employee preferences
 * (preferred day off or preferred shift template).
 */
@Component
public class ShiftAssignmentPreferenceAdvisor {

    /**
     * Returns warning messages if the assignment conflicts with the employee's preferences.
     * Does not block the assignment; for informational use only.
     */
    public List<String> getWarnings(Employee employee, ShiftInstance shiftInstance) {
        List<String> warnings = new ArrayList<>();

        if (employee.getPreferredDayOff() != null
                && shiftInstance.getDate().getDayOfWeek() == employee.getPreferredDayOff()) {
            warnings.add(String.format("Assignment is on employee's preferred day off (%s).",
                    employee.getPreferredDayOff()));
        }

        if (employee.getShiftPreferences() != null && !employee.getShiftPreferences().isEmpty()) {
            Long templateId = shiftInstance.getShiftTemplate().getId();
            boolean isPreferred = employee.getShiftPreferences().stream()
                    .map(EmployeeShiftPreference::getShiftTemplate)
                    .anyMatch(t -> t.getId().equals(templateId));
            if (!isPreferred) {
                warnings.add("Shift template is not among employee's preferred shifts.");
            }
        }

        return warnings;
    }
}
