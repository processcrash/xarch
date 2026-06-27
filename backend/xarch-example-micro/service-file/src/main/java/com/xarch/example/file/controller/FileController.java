package com.xarch.example.file.controller;

import com.xarch.example.file.entity.Resource;
import com.xarch.example.file.entity.StorageConfig;
import com.xarch.example.file.service.ResourceService;
import com.xarch.example.file.service.StorageConfigService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * File management controller — unified file operations across storage backends.
 *
 * <p>Migrated verbatim from monolith. Storage strategy objects
 * (StorageFactory / StorageStrategy / StorageType) are stubbed here as
 * a placeholder list of supported types — the full implementation will
 * be supplied as the storage subsystem is decomposed.
 */
@Tag(name = "File Management", description = "Unified file management with multi-storage support")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final ResourceService resourceService;
    private final StorageConfigService storageConfigService;

    @GetMapping("/page")
    @Operation(summary = "Page query files")
    public ApiResult<PageResult<Resource>> page(
            @RequestParam(required = false) String sceneCode,
            @RequestParam(required = false) String storageType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.success(resourceService.page(sceneCode, storageType, keyword, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get file details")
    public ApiResult<Resource> detail(@PathVariable Long id) {
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return ApiResult.fail("File not found");
        }
        return ApiResult.success(resource);
    }

    @PostMapping("/upload")
    @Operation(summary = "Upload file")
    public ApiResult<Resource> upload(
            @RequestParam(required = false) String sceneCode,
            @RequestParam(required = false) String bizKey,
            @RequestParam(required = false) String storageType,
            @RequestParam("file") MultipartFile file) {
        try {
            Resource resource = resourceService.upload(sceneCode, bizKey, storageType, file, null, null);
            return ApiResult.success(resource);
        } catch (IOException e) {
            return ApiResult.fail("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/download/{id}")
    @Operation(summary = "Download file")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable Long id) {
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            boolean success = resourceService.download(id, outputStream);
            if (!success) {
                return ResponseEntity.notFound().build();
            }
            byte[] bytes = outputStream.toByteArray();
            org.springframework.core.io.ByteArrayResource resourceBytes =
                    new org.springframework.core.io.ByteArrayResource(bytes);
            String filename = URLEncoder.encode(resource.getResourceName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(resource.getFileType() != null ? resource.getFileType() : "application/octet-stream"))
                    .contentLength(bytes.length)
                    .body(resourceBytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/preview/{id}")
    @Operation(summary = "Preview file (inline)")
    public ResponseEntity<InputStreamResource> preview(@PathVariable Long id) {
        Resource resource = resourceService.getById(id);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        InputStream inputStream = resourceService.getFileStream(id);
        if (inputStream == null) {
            return ResponseEntity.notFound().build();
        }
        String contentType = resource.getFileType() != null ? resource.getFileType() : "application/octet-stream";
        boolean inline = contentType.startsWith("image/") || contentType.startsWith("video/") || contentType.contains("pdf");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, inline ? "inline" : "attachment")
                .body(new InputStreamResource(inputStream));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete file (soft delete)")
    public ApiResult<Void> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return ApiResult.success(null);
    }

    @GetMapping("/stats")
    @Operation(summary = "Get storage statistics")
    public ApiResult<ResourceService.StorageStats> stats() {
        return ApiResult.success(resourceService.getStats());
    }

    @GetMapping("/storage/configs")
    @Operation(summary = "List storage configurations")
    public ApiResult<List<StorageConfig>> listStorageConfigs() {
        return ApiResult.success(storageConfigService.listEnabled());
    }

    @GetMapping("/storage/config/{id}")
    @Operation(summary = "Get storage configuration")
    public ApiResult<StorageConfig> getStorageConfig(@PathVariable Long id) {
        StorageConfig config = storageConfigService.getById(id);
        if (config == null) {
            return ApiResult.fail("Configuration not found");
        }
        if (config.getSecretKey() != null) {
            config.setSecretKey("********");
        }
        return ApiResult.success(config);
    }

    @PostMapping("/storage/config")
    @Operation(summary = "Create storage configuration")
    public ApiResult<Void> createStorageConfig(@RequestBody StorageConfig config) {
        storageConfigService.create(config);
        return ApiResult.success(null);
    }

    @PutMapping("/storage/config")
    @Operation(summary = "Update storage configuration")
    public ApiResult<Void> updateStorageConfig(@RequestBody StorageConfig config) {
        storageConfigService.update(config);
        return ApiResult.success(null);
    }

    @DeleteMapping("/storage/config/{id}")
    @Operation(summary = "Delete storage configuration")
    public ApiResult<Void> deleteStorageConfig(@PathVariable Long id) {
        storageConfigService.delete(id);
        return ApiResult.success(null);
    }

    @PostMapping("/storage/config/{id}/test")
    @Operation(summary = "Test storage connection")
    public ApiResult<Boolean> testStorageConfig(@PathVariable Long id) {
        StorageConfig config = storageConfigService.getById(id);
        if (config == null) {
            return ApiResult.fail("Configuration not found");
        }
        return ApiResult.success(storageConfigService.testConnection(config));
    }

    @GetMapping("/storage/types")
    @Operation(summary = "List available storage types")
    public ApiResult<List<StorageTypeVO>> listStorageTypes() {
        // Storage strategy enum is supplied by the storage subsystem;
        // listing here intentionally hard-codes the canonical codes.
        List<StorageTypeVO> types = List.of(
                new StorageTypeVO("local", "Local", "Local storage"),
                new StorageTypeVO("minio", "MinIO", "MinIO object storage"),
                new StorageTypeVO("aliyun_oss", "Aliyun OSS", "Aliyun OSS object storage")
        );
        return ApiResult.success(types);
    }

    /** Storage type view object. */
    public static class StorageTypeVO {
        private String code;
        private String name;
        private String description;

        public StorageTypeVO() {}

        public StorageTypeVO(String code, String name, String description) {
            this.code = code;
            this.name = name;
            this.description = description;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}