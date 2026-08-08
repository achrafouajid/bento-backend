package com.bento.crm.invoice.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "recovery_reminder")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecoveryReminder extends BaseTenantEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID invoiceId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID partnerId;

    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Column(columnDefinition = "uuid")
    private UUID templateId;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Instant sentAt;

    public enum Channel {
        EMAIL, SMS, WHATSAPP
    }

    public enum Status {
        SCHEDULED, SENT, FAILED
    }
}
