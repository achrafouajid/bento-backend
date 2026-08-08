package com.bento.crm.identity.repository;

import com.bento.crm.identity.model.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    @Query("SELECT u FROM AppUser u WHERE u.organizationId = :organizationId AND u.email = :email")
    Optional<AppUser> findByOrganizationIdAndEmail(@Param("organizationId") UUID organizationId, @Param("email") String email);

    @Query("SELECT u FROM AppUser u WHERE u.organizationId = :organizationId AND u.isActive = true")
    List<AppUser> findActiveByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("SELECT COUNT(u) FROM AppUser u WHERE u.organizationId = :organizationId AND u.role = 'ADMIN' AND u.isActive = true")
    long countActiveAdminsByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("SELECT u FROM AppUser u WHERE u.organizationId = :organizationId")
    List<AppUser> findByOrganizationId(@Param("organizationId") UUID organizationId);

    @Query("SELECT u FROM AppUser u WHERE u.organizationId = :organizationId")
    Page<AppUser> findByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Query("SELECT u FROM AppUser u WHERE u.organizationId = :organizationId AND u.id = :id")
    Optional<AppUser> findByOrganizationIdAndId(@Param("organizationId") UUID organizationId, @Param("id") UUID id);
}
