package com.bento.crm.automation.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "webhook_subscription")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookSubscription extends BaseTenantEntity {

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String secret;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] eventTypes;

    private Boolean isActive;
}
