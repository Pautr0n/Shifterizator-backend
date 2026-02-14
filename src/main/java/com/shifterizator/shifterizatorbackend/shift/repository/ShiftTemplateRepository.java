package com.shifterizator.shifterizatorbackend.shift.repository;

import com.shifterizator.shifterizatorbackend.shift.model.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long>, JpaSpecificationExecutor<ShiftTemplate> {

    List<ShiftTemplate> findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByStartTimeAsc(Long locationId);
    List<ShiftTemplate> findByLocation_IdAndDeletedAtIsNullAndIsActiveTrueOrderByPriorityAscStartTimeAsc(Long locationId);

}
