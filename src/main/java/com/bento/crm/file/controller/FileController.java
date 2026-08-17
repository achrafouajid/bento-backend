package com.bento.crm.file.controller;

import com.bento.crm.file.dto.StoredFileResponse;
import com.bento.crm.file.model.StoredFile;
import com.bento.crm.file.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "Generic file attachment storage endpoints")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload file", description = "Upload a file attached to an owner entity")
    public ResponseEntity<StoredFileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("ownerEntityType") String ownerEntityType,
            @RequestParam("ownerEntityId") UUID ownerEntityId) {
        StoredFile storedFile = fileStorageService.store(file, ownerEntityType, ownerEntityId);
        return ResponseEntity.status(HttpStatus.CREATED).body(StoredFileResponse.fromEntity(storedFile));
    }

    @GetMapping
    @Operation(summary = "List files for owner", description = "List files attached to an owner entity")
    public ResponseEntity<List<StoredFileResponse>> listFiles(
            @RequestParam("ownerEntityType") String ownerEntityType,
            @RequestParam("ownerEntityId") UUID ownerEntityId) {
        List<StoredFileResponse> files = fileStorageService.listByOwner(ownerEntityType, ownerEntityId).stream()
                .map(StoredFileResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Download file", description = "Download a previously uploaded file")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) {
        StoredFile storedFile = fileStorageService.getMetadata(id);
        Resource resource = fileStorageService.load(id);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(storedFile.getContentType());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + storedFile.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete file", description = "Delete a previously uploaded file")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id) {
        fileStorageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
