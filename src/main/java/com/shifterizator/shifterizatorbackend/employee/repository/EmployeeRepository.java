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
}
