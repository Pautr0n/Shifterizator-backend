package com.shifterizator.shifterizatorbackend.company.repository;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    public Optional<Company> findByName(String name);

    public Optional<Company> findByTaxId(String taxId);

    public Optional<Company> findByEmail(String email);

    public List<Company> findByIsActive(Boolean isActive);

    public List<Company> findByNameContainingIgnoreCase(String name);

    public List<Company> findByNameContainingIgnoreCaseAndIsActive(String name, boolean isActive);

    public boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    public boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    public boolean existsByTaxIdIgnoreCaseAndIdNot(String taxId, Long id);

}
