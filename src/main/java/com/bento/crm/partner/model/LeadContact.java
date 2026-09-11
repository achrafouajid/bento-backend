package com.bento.crm.partner.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "lead_contact")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadContact extends BaseTenantEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID partnerId;

    @Column(nullable = false)
    private String name;

    private String jobTitle;

    private String email;

    private String phone;

    private String mobile;

    private String website;

    private String linkedin;
}
