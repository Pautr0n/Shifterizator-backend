package com.shifterizator.shifterizatorbackend.shift.repository;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long>, JpaSpecificationExecutor<ShiftAssignment> {

    List<ShiftAssignment> findByShiftInstance_IdAndDeletedAtIsNull(Long shiftInstanceId);

    List<ShiftAssignment> findByEmployee_IdAndDeletedAtIsNullOrderByShiftInstance_DateAscShiftInstance_StartTimeAsc(Long employeeId);

    @Query("""
            SELECT sa FROM ShiftAssignment sa
            WHERE sa.employee.id = :employeeId
              AND sa.shiftInstance.date = :date
              AND sa.deletedAt IS NULL
            """)
    List<ShiftAssignment> findByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);

    Optional<ShiftAssignment> findByShiftInstance_IdAndEmployee_IdAndDeletedAtIsNull(Long shiftInstanceId, Long employeeId);
}
