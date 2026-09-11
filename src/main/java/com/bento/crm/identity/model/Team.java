package com.bento.crm.identity.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "team")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team extends BaseTenantEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private TeamDepartment department;

    private String description;

    @Column(columnDefinition = "uuid")
    private UUID leadUserId;

    @Column(length = 20)
    private String color;

    public enum TeamDepartment {
        SALES, OPERATIONS, FINANCE, SUPPORT, CUSTOM
    }
}
