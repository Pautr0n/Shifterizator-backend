package com.shifterizator.shifterizatorbackend.blackoutdays.repository;

import com.shifterizator.shifterizatorbackend.blackoutdays.model.BlackoutDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BlackoutDayRepository extends JpaRepository<BlackoutDay, Long>, JpaSpecificationExecutor<BlackoutDay> {

    List<BlackoutDay> findByLocation_IdAndDeletedAtIsNullOrderByDateAsc(Long locationId);

    List<BlackoutDay> findByLocation_Company_IdAndDeletedAtIsNullOrderByDateAsc(Long companyId);

    @Query("""
            SELECT COUNT(soh) > 0 FROM com.shifterizator.shifterizatorbackend.openinghours.model.SpecialOpeningHours soh
            WHERE soh.deletedAt IS NULL
              AND soh.location.id = :locationId
              AND soh.date = :date
            """)
    boolean existsSpecialOpeningHoursForLocationAndDate(@Param("locationId") Long locationId, @Param("date") LocalDate date);
}
