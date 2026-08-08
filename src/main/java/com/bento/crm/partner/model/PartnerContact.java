package com.bento.crm.partner.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "partner_contact")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerContact extends BaseTenantEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID partnerId;

    private String fullName;

    private String jobTitle;

    private String mobile;

    private String email;

    private Boolean isPrimary;

    private Boolean isDecisionMaker;

    private Boolean isTechnicalContact;

    private Boolean isFinanceContact;
}
