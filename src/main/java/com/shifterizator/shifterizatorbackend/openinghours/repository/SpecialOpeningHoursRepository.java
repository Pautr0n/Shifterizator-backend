package com.shifterizator.shifterizatorbackend.openinghours.repository;

import com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SpecialOpeningHoursRepository extends JpaRepository<SpecialOpeningHours, Long>, JpaSpecificationExecutor<SpecialOpeningHours> {

    List<SpecialOpeningHours> findByLocation_IdAndDeletedAtIsNullOrderByDateAsc(Long locationId);

    List<SpecialOpeningHours> findByLocation_Company_IdAndDeletedAtIsNullOrderByDateAsc(Long companyId);
}
