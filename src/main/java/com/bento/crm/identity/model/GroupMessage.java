package com.bento.crm.identity.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "group_message")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMessage extends BaseTenantEntity {

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID groupId;

    @Column(nullable = false, columnDefinition = "uuid")
    private UUID authorUserId;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "uuid[]")
    private List<UUID> readByUserIds;
}
