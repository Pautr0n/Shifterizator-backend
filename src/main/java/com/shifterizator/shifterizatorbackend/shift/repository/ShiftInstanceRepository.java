package com.shifterizator.shifterizatorbackend.shift.repository;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ShiftInstanceRepository extends JpaRepository<ShiftInstance, Long>, JpaSpecificationExecutor<ShiftInstance> {

    List<ShiftInstance> findByLocation_IdAndDateAndDeletedAtIsNullOrderByStartTimeAsc(Long locationId, LocalDate date);

    List<ShiftInstance> findByLocation_IdAndDateBetweenAndDeletedAtIsNullOrderByDateAscStartTimeAsc(Long locationId, LocalDate startDate, LocalDate endDate);

    @Query("""
            SELECT COUNT(sa) FROM ShiftAssignment sa
            WHERE sa.shiftInstance.id = :shiftInstanceId
              AND sa.deletedAt IS NULL
            """)
    int countActiveAssignments(@Param("shiftInstanceId") Long shiftInstanceId);
}
