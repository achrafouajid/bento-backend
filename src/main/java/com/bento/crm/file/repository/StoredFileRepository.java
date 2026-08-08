package com.bento.crm.file.repository;

import com.bento.crm.file.model.StoredFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {

    @Query("SELECT f FROM StoredFile f WHERE f.organizationId = :orgId AND f.id = :id")
    Optional<StoredFile> findByOrganizationIdAndId(@Param("orgId") UUID orgId, @Param("id") UUID id);
}
