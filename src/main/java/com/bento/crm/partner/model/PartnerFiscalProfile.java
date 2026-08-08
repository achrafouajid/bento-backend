package com.bento.crm.partner.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "partner_fiscal_profile", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"organization_id", "partner_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerFiscalProfile extends BaseTenantEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID partnerId;

    private String accountId;

    private String erpAccount;

    private String ice;

    private String ifField;

    private String rc;

    private String rcCity;

    private String tp;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] vatStatus;

    @Enumerated(EnumType.STRING)
    private OrgType orgType;

    @Column(columnDefinition = "uuid")
    private UUID parentAccountPartnerId;

    public enum OrgType {
        HEADQUARTER, SUBSIDIARY, BRANCH
    }
}
