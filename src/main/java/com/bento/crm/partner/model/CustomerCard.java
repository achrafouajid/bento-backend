package com.bento.crm.partner.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "customer_card")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCard extends BaseTenantEntity {

    @Column(nullable = false, unique = true, columnDefinition = "uuid")
    private UUID partnerId;

    private String accountId;

    @Enumerated(EnumType.STRING)
    private RecordType recordType;

    @Column(nullable = false)
    private String name;

    private String searchName;

    private String erpAccount;

    private String ice;

    @Column(name = "if_number")
    private String ifNumber;

    private String rc;

    private String rcCity;

    private String tp;

    // NOT NULL in the schema; a card created with only the fiscal identifiers has no statuses yet.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<String> vatStatus = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrgType orgType;

    @Column(columnDefinition = "uuid")
    private UUID parentAccountId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, Object>> addresses = new ArrayList<>();

    private String mainPhone;

    private String corporateEmail;

    private String websiteUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private List<Map<String, Object>> personnel = new ArrayList<>();

    public enum RecordType {
        ORGANIZATION, INDIVIDUAL
    }

    public enum OrgType {
        HEADQUARTER, SUBSIDIARY, BRANCH
    }
}
