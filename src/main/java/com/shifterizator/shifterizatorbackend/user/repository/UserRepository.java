package com.shifterizator.shifterizatorbackend.user.repository;

import com.shifterizator.shifterizatorbackend.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNullAndIdNot(String email, Long id);

    List<User> findByUsernameContainingIgnoreCase(String username);

    List<User> findByEmailContainingIgnoreCaseAndDeletedAtIsNull(String email);

    List<User> findByIsActive(Boolean isActive);

    List<User> findByUsernameContainingIgnoreCaseAndIsActive(String username, Boolean isActive);

    List<User> findByCompany_IdAndDeletedAtIsNull(Long companyId);

    /** Users whose company is in the given set, not deleted, active. For assignable-user dropdowns. */
    List<User> findByCompany_IdInAndDeletedAtIsNullAndIsActiveTrue(Set<Long> companyIds);

}
