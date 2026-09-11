package com.bento.crm.identity.repository;

import com.bento.crm.identity.model.Team;
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
public interface TeamRepository extends JpaRepository<Team, UUID> {

    @Query("SELECT t FROM Team t WHERE t.organizationId = :organizationId")
    Page<Team> findByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Query("SELECT t FROM Team t WHERE t.organizationId = :organizationId AND t.id = :id")
    Optional<Team> findByOrganizationIdAndId(@Param("organizationId") UUID organizationId, @Param("id") UUID id);

    @Query("SELECT t FROM Team t WHERE t.organizationId = :organizationId")
    List<Team> findAllByOrganizationId(@Param("organizationId") UUID organizationId);
}
