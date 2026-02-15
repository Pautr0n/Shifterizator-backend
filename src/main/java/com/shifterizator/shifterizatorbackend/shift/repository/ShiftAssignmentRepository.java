package com.shifterizator.shifterizatorbackend.shift.repository;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    List<ShiftAssignment> findByEmployee_IdAndShiftInstance_DateBetweenAndDeletedAtIsNull(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT COUNT(sa) FROM ShiftAssignment sa
            WHERE sa.employee.id = :employeeId
              AND sa.shiftInstance.date BETWEEN :startDate AND :endDate
              AND sa.deletedAt IS NULL
            """)
    long countByEmployee_IdAndShiftInstance_DateBetweenAndDeletedAtIsNull(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Optional<ShiftAssignment> findByShiftInstance_IdAndEmployee_IdAndDeletedAtIsNull(Long shiftInstanceId, Long employeeId);

    @Query("""
            SELECT sa.employee.id FROM ShiftAssignment sa
            WHERE sa.shiftInstance.id IN :shiftInstanceIds
              AND sa.deletedAt IS NULL
            """)
    List<Long> findAssignedEmployeeIdsByShiftInstanceIdIn(@Param("shiftInstanceIds") List<Long> shiftInstanceIds);

    @Query("""
            SELECT sa FROM ShiftAssignment sa
            LEFT JOIN FETCH sa.shiftInstance si
            LEFT JOIN FETCH si.location
            LEFT JOIN FETCH sa.employee e
            LEFT JOIN FETCH e.user
            WHERE sa.id = :id
            """)
    Optional<ShiftAssignment> findByIdWithShiftInstanceAndEmployeeUser(@Param("id") Long id);

    @Modifying
    @Query("""
                UPDATE ShiftAssignment sa
                SET sa.deletedAt = :deletedAt
                WHERE sa.shiftInstance.id = :shiftInstanceId
                  AND sa.deletedAt IS NULL
            """)
    int softDeleteByShiftInstanceId(@Param("shiftInstanceId") Long shiftInstanceId,
                                    @Param("deletedAt") LocalDateTime deletedAt);

    @Modifying
    @Query("""
                UPDATE ShiftAssignment sa
                SET sa.deletedAt = :deletedAt
                WHERE sa.shiftInstance.id IN :shiftInstanceIds
                  AND sa.deletedAt IS NULL
            """)
    int softDeleteByShiftInstanceIds(@Param("shiftInstanceIds") List<Long> shiftInstanceIds,
                                     @Param("deletedAt") LocalDateTime deletedAt);


    @Modifying
    @Query("""
                UPDATE ShiftAssignment sa
                SET sa.deletedAt = :deletedAt
                WHERE sa.shiftInstance.location.id = :locationId
                  AND sa.shiftInstance.date = :date
                  AND sa.deletedAt IS NULL
            """)
    int softDeleteByLocationAndDate(@Param("locationId") Long locationId,
                                    @Param("date") LocalDate date,
                                    @Param("deletedAt") LocalDateTime deletedAt);

}
