package com.shifterizator.shifterizatorbackend.availability.repository;

import com.shifterizator.shifterizatorbackend.availability.model.EmployeeAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeAvailabilityRepository extends JpaRepository<EmployeeAvailability, Long>, JpaSpecificationExecutor<EmployeeAvailability> {

    List<EmployeeAvailability> findByEmployee_IdAndDeletedAtIsNullOrderByStartDateAsc(Long employeeId);

    @Query("""
            SELECT ea FROM EmployeeAvailability ea
            WHERE ea.deletedAt IS NULL
              AND ea.employee.id = :employeeId
              AND (ea.startDate <= :end AND ea.endDate >= :start)
              AND (ea.id <> :excludeId OR :excludeId IS NULL)
            """)
    List<EmployeeAvailability> findOverlapping(
            @Param("employeeId") Long employeeId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("excludeId") Long excludeId
    );
}
