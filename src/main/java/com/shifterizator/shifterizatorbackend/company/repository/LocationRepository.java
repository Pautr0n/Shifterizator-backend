package com.shifterizator.shifterizatorbackend.company.repository;

import com.shifterizator.shifterizatorbackend.company.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByCompany_Id(Long companyId);
}
