package com.shifterizator.shifterizatorbackend.shift.service.advisor;

import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import com.shifterizator.shifterizatorbackend.employee.model.EmployeeShiftPreference;
import com.shifterizator.shifterizatorbackend.employee.repository.EmployeeLanguageRepository;
import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Assigns a tier (1=best .. 5=fallback) to a candidate for a given shift and date.
 * Used by the scheduler to order candidates: Tier 1 first, then 2a, 2b, 2c, then 3.
 */
@Service
@RequiredArgsConstructor
public class ShiftCandidateTierService {

    private final EmployeeLanguageRepository employeeLanguageRepository;

    /** Lower = better. 1 = Tier 1, 2 = 2a, 3 = 2b, 4 = 2c, 5 = Tier 3. */
    public int getTier(Employee employee, ShiftInstance shiftInstance, LocalDate date) {
        boolean notPreferredDayOff = employee.getPreferredDayOff() == null
                || date.getDayOfWeek() != employee.getPreferredDayOff();
        boolean positionMatch = matchesPosition(employee, shiftInstance);
        boolean languageMatch = meetsLanguageRequirements(employee, shiftInstance);
        boolean shiftInPreferences = hasShiftTemplateInPreferences(employee, shiftInstance);

        if (notPreferredDayOff && shiftInPreferences && languageMatch && positionMatch) {
            return 1;
        }
        if (positionMatch && notPreferredDayOff && shiftInPreferences) {
            return 2;
        }
        if (positionMatch && notPreferredDayOff) {
            return 3;
        }
        if (notPreferredDayOff) {
            return 4;
        }
        return 5;
    }

    private boolean matchesPosition(Employee employee, ShiftInstance shiftInstance) {
        if (employee.getPosition() == null) {
            return false;
        }
        Long employeePositionId = employee.getPosition().getId();
        return shiftInstance.getShiftTemplate().getRequiredPositions().stream()
                .anyMatch(stp -> stp.getPosition().getId().equals(employeePositionId));
    }

    private boolean meetsLanguageRequirements(Employee employee, ShiftInstance shiftInstance) {
        Set<Long> requiredIds = shiftInstance.getShiftTemplate().getRequiredLanguages().stream()
                .map(lang -> lang.getId())
                .collect(Collectors.toSet());
        if (requiredIds.isEmpty()) {
            return true;
        }
        Set<Long> employeeLanguageIds = employeeLanguageRepository.findByEmployee_Id(employee.getId()).stream()
                .map(el -> el.getLanguage().getId())
                .collect(Collectors.toSet());

        return requiredIds.stream().anyMatch(employeeLanguageIds::contains);
    }

    private boolean hasShiftTemplateInPreferences(Employee employee, ShiftInstance shiftInstance) {
        if (employee.getShiftPreferences() == null || employee.getShiftPreferences().isEmpty()) {
            return false;
        }
        Long templateId = shiftInstance.getShiftTemplate().getId();
        return employee.getShiftPreferences().stream()
                .map(EmployeeShiftPreference::getShiftTemplate)
                .anyMatch(t -> t.getId().equals(templateId));
    }
}