package com.bento.crm.automation.repository;

import com.bento.crm.automation.model.AutomationRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, UUID> {

    @Query("SELECT ar FROM AutomationRule ar WHERE ar.organizationId = :organizationId")
    Page<AutomationRule> findByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Query("SELECT ar FROM AutomationRule ar WHERE ar.organizationId = :organizationId AND ar.id = :id")
    Optional<AutomationRule> findByOrganizationIdAndId(@Param("organizationId") UUID organizationId, @Param("id") UUID id);
}
