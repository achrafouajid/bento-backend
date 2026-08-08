package com.bento.crm.file.service;

import com.bento.crm.file.model.StoredFile;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface FileStorageService {

    StoredFile store(MultipartFile file, String ownerEntityType, UUID ownerEntityId);

    StoredFile getMetadata(UUID fileId);

    Resource load(UUID fileId);

    void delete(UUID fileId);
}
