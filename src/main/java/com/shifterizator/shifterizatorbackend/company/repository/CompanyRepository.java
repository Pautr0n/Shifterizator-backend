package com.shifterizator.shifterizatorbackend.company.repository;

import com.shifterizator.shifterizatorbackend.company.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {

    Optional<Company> findByIdAndDeletedAtIsNull(Long id);

    Optional<Company> findByNameAndDeletedAtIsNull(String name);

    Optional<Company> findByTaxIdAndDeletedAtIsNull(String taxId);

    Optional<Company> findByEmailAndDeletedAtIsNull(String email);

    List<Company> findByDeletedAtIsNull();

    List<Company> findByIsActiveAndDeletedAtIsNull(Boolean isActive);

    List<Company> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String name);

    List<Company> findByNameContainingIgnoreCaseAndIsActiveAndDeletedAtIsNull(String name, boolean isActive);

    boolean existsByNameIgnoreCaseAndIdNotAndDeletedAtIsNull(String name, Long id);

    boolean existsByEmailIgnoreCaseAndIdNotAndDeletedAtIsNull(String email, Long id);

    boolean existsByTaxIdIgnoreCaseAndIdNotAndDeletedAtIsNull(String taxId, Long id);
}
