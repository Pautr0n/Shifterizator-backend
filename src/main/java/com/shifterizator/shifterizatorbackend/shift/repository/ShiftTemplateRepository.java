package com.shifterizator.shifterizatorbackend.shift.repository;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long>, JpaSpecificationExecutor<ShiftTemplate> {

    List<ShiftTemplate> findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByStartTimeAsc(Long locationId);
    List<ShiftTemplate> findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByPriorityAscStartTimeAsc(Long locationId);

    /** Eagerly loads requiredPositions and each position so update does not insert duplicate rows. */
    @Query("""
            SELECT DISTINCT t FROM ShiftTemplate t
            LEFT JOIN FETCH t.requiredPositions rp
            LEFT JOIN FETCH rp.position
            WHERE t.id = :id AND t.deletedAt IS NULL
            """)
    List<ShiftTemplate> findByIdWithRequiredPositions(@Param("id") Long id);
}
