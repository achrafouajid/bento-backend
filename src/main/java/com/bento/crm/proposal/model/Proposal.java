package com.bento.crm.proposal.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "proposal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proposal extends BaseTenantEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID partnerId;

    @Column(columnDefinition = "uuid")
    private UUID templateId;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProposalStatus status;

    private String deliveryMethod;

    private BigDecimal opportunityValue;

    private Integer closingProbability;

    private LocalDate expectedClosingDate;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] competitors;

    private String confirmationMethod;

    private String confirmationAttachmentFileId;

    @Column(columnDefinition = "text")
    private String confirmationNote;

    private Instant confirmedAt;

    private Instant sentAt;

    public enum ProposalStatus {
        DRAFT, SENT, CONFIRMED, REJECTED, EXPIRED
    }
}
