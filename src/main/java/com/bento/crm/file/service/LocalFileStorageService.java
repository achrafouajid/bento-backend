package com.bento.crm.file.service;

import com.bento.crm.common.context.TenantContext;
import com.bento.crm.common.exception.ResourceNotFoundException;
import com.bento.crm.file.model.StoredFile;
import com.bento.crm.file.repository.StoredFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    @Value("${FILE_STORAGE_PATH:/data/uploads}")
    private String storagePath;

    private final StoredFileRepository fileRepository;
    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024; // 15 MB
    private static final String[] ALLOWED_TYPES = {"image/", "application/pdf", "text/plain", "application/vnd.ms-excel", "application/vnd.openxmlformats"};

    @Override
    public StoredFile store(MultipartFile file, String ownerEntityType, UUID ownerEntityId) {
        validateFile(file);

        UUID orgId = TenantContext.getCurrentOrganizationId();
        UUID fileId = UUID.randomUUID();

        try {
            String filePath = String.format("%s/%s/%s/%s_%s", storagePath, orgId, ownerEntityType, fileId, file.getOriginalFilename());
            Path path = Paths.get(filePath);

            Files.createDirectories(path.getParent());
            Files.write(path, file.getBytes());

            StoredFile storedFile = StoredFile.builder()
                    .ownerEntityId(ownerEntityId)
                    .ownerEntityType(ownerEntityType)
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .storagePath(filePath)
                    .uploadedAt(Instant.now())
                    .build();
            storedFile.setOrganizationId(orgId);

            return fileRepository.save(storedFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public StoredFile getMetadata(UUID fileId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        return fileRepository.findByOrganizationIdAndId(orgId, fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
    }

    @Override
    public Resource load(UUID fileId) {
        StoredFile storedFile = getMetadata(fileId);

        Path path = Paths.get(storedFile.getStoragePath());
        if (Files.exists(path)) {
            return new FileSystemResource(path);
        }
        throw new ResourceNotFoundException("File not found in storage");
    }

    @Override
    public void delete(UUID fileId) {
        UUID orgId = TenantContext.getCurrentOrganizationId();
        StoredFile storedFile = fileRepository.findByOrganizationIdAndId(orgId, fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        try {
            Files.delete(Paths.get(storedFile.getStoragePath()));
            fileRepository.delete(storedFile);
        } catch (IOException e) {
            log.error("Failed to delete file", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File exceeds maximum size of 15 MB");
        }

        String contentType = file.getContentType();
        boolean allowed = false;
        for (String type : ALLOWED_TYPES) {
            if (contentType != null && contentType.startsWith(type)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new IllegalArgumentException("File type not allowed");
        }
    }
}
