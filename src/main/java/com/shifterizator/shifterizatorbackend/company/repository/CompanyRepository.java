package com.shifterizator.shifterizatorbackend.company.repository;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {

    Optional<Company> findByName(String name);

    Optional<Company> findByTaxId(String taxId);

    Optional<Company> findByEmail(String email);

    List<Company> findByIsActive(Boolean isActive);

    List<Company> findByNameContainingIgnoreCase(String name);

    List<Company> findByNameContainingIgnoreCaseAndIsActive(String name, boolean isActive);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByTaxIdIgnoreCaseAndIdNot(String taxId, Long id);

}
