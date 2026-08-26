package com.bento.crm.task.repository;

import com.bento.crm.task.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

    @Query("SELECT t FROM Task t WHERE t.organizationId = :organizationId")
    Page<Task> findByOrganizationId(@Param("organizationId") UUID organizationId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.organizationId = :organizationId AND t.id = :id")
    Optional<Task> findByOrganizationIdAndId(@Param("organizationId") UUID organizationId, @Param("id") UUID id);
}
