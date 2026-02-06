package com.shifterizator.shifterizatorbackend.employee.service;

import com.shifterizator.shifterizatorbackend.employee.model.Position;

import java.util.List;

public interface PositionService {

    Position create(String name, Long companyId);

    Position update(Long id, String name);

    void delete(Long id);

    Position findById(Long id);

    List<Position> findByCompany(Long companyId);

}
