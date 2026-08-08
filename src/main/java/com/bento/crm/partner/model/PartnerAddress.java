package com.bento.crm.partner.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "partner_address")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerAddress extends BaseTenantEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID partnerId;

    @Enumerated(EnumType.STRING)
    private AddressType addressType;

    private String streetAddress;

    private String industrialZone;

    private String postalCode;

    private String city;

    private String country;

    private Boolean isPrimary;

    public enum AddressType {
        REGISTERED_OFFICE, DELIVERY, WAREHOUSE, BILLING
    }
}
