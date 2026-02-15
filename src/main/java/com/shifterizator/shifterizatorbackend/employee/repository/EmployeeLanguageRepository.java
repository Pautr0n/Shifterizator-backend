package com.shifterizator.shifterizatorbackend.employee.repository;

import com.shifterizator.shifterizatorbackend.employee.model.EmployeeLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeLanguageRepository extends JpaRepository<EmployeeLanguage, Long> {

    List<EmployeeLanguage> findByEmployee_Id(Long employeeId);

    List<EmployeeLanguage> findByEmployee_IdIn(List<Long> employeeIds);

    @Modifying
    @Query("DELETE FROM EmployeeLanguage el WHERE el.employee.id = :employeeId")
    void deleteByEmployee_Id(@Param("employeeId") Long employeeId);
}
