package com.bento.crm.identity.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "crm_group")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmGroup extends BaseTenantEntity {

    @Column(nullable = false)
    private String name;

    private String description;
}
