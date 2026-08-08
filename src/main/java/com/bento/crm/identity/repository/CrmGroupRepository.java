package com.bento.crm.identity.repository;

import com.bento.crm.identity.model.CrmGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CrmGroupRepository extends JpaRepository<CrmGroup, UUID> {

    @Query("SELECT g FROM CrmGroup g WHERE g.organizationId = :organizationId")
    Page<CrmGroup> findByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Query("SELECT g FROM CrmGroup g WHERE g.organizationId = :organizationId AND g.id = :id")
    Optional<CrmGroup> findByOrganizationIdAndId(@Param("organizationId") UUID organizationId, @Param("id") UUID id);
}
