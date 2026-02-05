package com.shifterizator.shifterizatorbackend.company.repository;

import com.shifterizator.shifterizatorbackend.company.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
