package com.bento.crm.proposal.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "proposal_template")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalTemplate extends BaseTenantEntity {

    @Column(nullable = false)
    private String name;

    private String subject;

    @Column(columnDefinition = "text")
    private String body;

    @Enumerated(EnumType.STRING)
    private Channel channel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> variables;

    private String imageFileId;

    private String approvalStatus;

    public enum Channel {
        PROPOSAL, WHATSAPP, SMS, EMAIL
    }
}
