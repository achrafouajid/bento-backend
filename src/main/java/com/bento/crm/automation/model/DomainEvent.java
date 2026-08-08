package com.bento.crm.automation.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "domain_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainEvent extends BaseTenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AutomationRule.Trigger eventType;

    private String entityType;

    @Column(columnDefinition = "uuid")
    private UUID entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    private Instant publishedAt;
}
