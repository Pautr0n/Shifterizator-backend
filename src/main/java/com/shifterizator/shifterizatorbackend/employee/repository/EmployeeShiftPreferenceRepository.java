package com.shifterizator.shifterizatorbackend.employee.repository;

import com.shifterizator.shifterizatorbackend.employee.model.EmployeeShiftPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeShiftPreferenceRepository extends JpaRepository<EmployeeShiftPreference, Long> {

    List<EmployeeShiftPreference> findByEmployee_Id(Long employeeId);

    List<EmployeeShiftPreference> findByEmployee_IdOrderByPriorityOrderAsc(Long employeeId);

    @Modifying
    @Query("DELETE FROM EmployeeShiftPreference esp WHERE esp.employee.id = :employeeId")
    void deleteByEmployee_Id(@Param("employeeId") Long employeeId);
}
