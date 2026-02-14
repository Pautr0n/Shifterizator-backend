package com.shifterizator.shifterizatorbackend.employee.repository;

import com.shifterizator.shifterizatorbackend.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    /**
     * Check if an email already exists for a given company.
     * Used to enforce "email unique per company" rule.
     */
    @Query("""
            SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END
            FROM Employee e
            JOIN e.employeeCompanies ec
            WHERE e.email = :email
              AND ec.company.id = :companyId
              AND e.deletedAt IS NULL
            """)
    boolean existsByEmailAndCompany(String email, Long companyId);


    /**
     * Find an employee by ID but only if not soft-deleted.
     */
    @Query("""
            SELECT e
            FROM Employee e
            WHERE e.id = :id
              AND e.deletedAt IS NULL
            """)
    Optional<Employee> findActiveById(Long id);


    /**
     * Check if an employee is assigned to any shift.
     * Returns false until Shift entity is added (Sprint 6). Replace with JPQL when Shift exists.
     */
    default boolean isEmployeeAssignedToAnyShift(Long employeeId) {
        return false;
    }

    /**
     * Employees that work at the given location and are not soft-deleted.
     * Used by the scheduler to build the candidate pool.
     */
    @Query("""
            SELECT DISTINCT e FROM Employee e
            JOIN e.employeeLocations el
            WHERE el.location.id = :locationId
              AND e.deletedAt IS NULL
            """)
    List<Employee> findActiveByLocationId(@Param("locationId") Long locationId);

    /**
     * Same as findActiveByLocationId but with shiftPreferences and their shiftTemplate loaded
     * for scheduler tier computation. Avoids N+1 when evaluating preferences.
     */
    @Query("""
        SELECT DISTINCT e FROM Employee e
        JOIN e.employeeLocations el
        LEFT JOIN FETCH e.shiftPreferences sp
        LEFT JOIN FETCH sp.shiftTemplate
        WHERE el.location.id = :locationId
          AND e.deletedAt IS NULL
        """)
    List<Employee> findActiveByLocationIdWithShiftPreferences(@Param("locationId") Long locationId);

    /**
     * Check if a user is already assigned to another employee.
     * Used to enforce "one user per employee" rule.
     */
    @Query("""
            SELECT e
            FROM Employee e
            WHERE e.user.id = :userId
              AND e.deletedAt IS NULL
            """)
    Optional<Employee> findByUserId(@Param("userId") Long userId);
}
