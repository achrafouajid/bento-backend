package com.bento.crm.common.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FilterDef(name = "organizationFilter", parameters = @org.hibernate.annotations.ParamDef(name = "organizationId", type = java.util.UUID.class))
@Filter(name = "organizationFilter", condition = "organization_id = :organizationId")
@Getter
@Setter
public abstract class BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID organizationId;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @CreatedBy
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID createdBy;

    @LastModifiedBy
    @Column(nullable = false, columnDefinition = "uuid")
    private UUID updatedBy;
}
