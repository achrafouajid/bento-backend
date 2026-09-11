package com.bento.crm.file.model;

import com.bento.crm.common.model.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "stored_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoredFile extends BaseTenantEntity {

    @Column(columnDefinition = "uuid")
    private UUID ownerEntityId;

    private String ownerEntityType;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    private Long sizeBytes;

    @Column(nullable = false)
    private String storagePath;

    @Column(columnDefinition = "uuid")
    private UUID uploadedByUserId;

    private java.time.Instant uploadedAt;
}
