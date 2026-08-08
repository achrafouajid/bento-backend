package com.bento.crm.file.dto;

import com.bento.crm.file.model.StoredFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoredFileResponse {

    private UUID id;
    private String fileName;
    private String contentType;
    private Long sizeBytes;
    private String ownerEntityType;
    private UUID ownerEntityId;
    private Instant uploadedAt;

    public static StoredFileResponse fromEntity(StoredFile file) {
        return StoredFileResponse.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .contentType(file.getContentType())
                .sizeBytes(file.getSizeBytes())
                .ownerEntityType(file.getOwnerEntityType())
                .ownerEntityId(file.getOwnerEntityId())
                .uploadedAt(file.getUploadedAt())
                .build();
    }
}
