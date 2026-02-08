package com.shifterizator.shifterizatorbackend.employee.repository;

import com.shifterizator.shifterizatorbackend.employee.model.EmployeeLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeLanguageRepository extends JpaRepository<EmployeeLanguage, Long> {

    List<EmployeeLanguage> findByEmployeeId(Long employeeId);

    void deleteByEmployeeId(Long employeeId);
}
