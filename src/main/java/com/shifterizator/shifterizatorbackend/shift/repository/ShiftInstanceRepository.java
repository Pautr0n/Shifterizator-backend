package com.shifterizator.shifterizatorbackend.shift.repository;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
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
public interface ShiftInstanceRepository extends JpaRepository<ShiftInstance, Long>, JpaSpecificationExecutor<ShiftInstance> {

    List<ShiftInstance> findByLocation_IdAndDateAndDeletedAtIsNullOrderByStartTimeAsc(Long locationId, LocalDate date);

    @Query("""
            SELECT DISTINCT i FROM ShiftInstance i
            LEFT JOIN FETCH i.shiftTemplate t
            LEFT JOIN FETCH t.requiredPositions
            WHERE i.location.id = :locationId AND i.date = :date AND i.deletedAt IS NULL
            ORDER BY i.startTime
            """)
    List<ShiftInstance> findByLocationIdAndDateWithTemplateAndPositions(@Param("locationId") Long locationId, @Param("date") LocalDate date);

    List<ShiftInstance> findByLocation_IdAndDateAndShiftTemplate_IdAndDeletedAtIsNull(Long locationId, LocalDate date, Long shiftTemplateId);

    List<ShiftInstance> findByLocation_IdAndDateBetweenAndDeletedAtIsNullOrderByDateAscStartTimeAsc(Long locationId, LocalDate startDate, LocalDate endDate);

    @Query("""
                SELECT DISTINCT i FROM ShiftInstance i
                LEFT JOIN FETCH i.shiftTemplate t
                LEFT JOIN FETCH t.requiredPositions rp
                LEFT JOIN FETCH rp.position p
                WHERE i.id = :id AND i.deletedAt IS NULL
            """)
    Optional<ShiftInstance> findByIdFullyLoaded(@Param("id") Long id);

    @Query("""
                SELECT i FROM ShiftInstance i
                LEFT JOIN FETCH i.shiftTemplate t
                LEFT JOIN FETCH t.requiredPositions rp
                WHERE i.id = :id AND i.deletedAt IS NULL
            """)
    Optional<ShiftInstance> findByIdWithTemplateAndPositions(@Param("id") Long id);

    @Query("""
            SELECT COUNT(sa) FROM ShiftAssignment sa
            WHERE sa.shiftInstance.id = :shiftInstanceId
              AND sa.deletedAt IS NULL
            """)
    int countActiveAssignments(@Param("shiftInstanceId") Long shiftInstanceId);

    @Modifying
    @Query("""
            UPDATE ShiftInstance i SET i.deletedAt = :deletedAt
            WHERE i.location.id = :locationId AND i.date = :date AND i.deletedAt IS NULL
            """)
    int softDeleteByLocationAndDate(@Param("locationId") Long locationId, @Param("date") LocalDate date, @Param("deletedAt") LocalDateTime deletedAt);
}
