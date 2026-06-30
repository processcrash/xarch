package com.xarch.example.service;

import com.xarch.example.entity.Resource;
import com.xarch.starter.storage.core.StorageException;
import com.xarch.starter.storage.core.StorageResult;
import com.xarch.starter.storage.core.StorageType;
import com.xarch.starter.storage.service.FileStorageService;
import com.xarch.starter.storage.service.UploadOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Duration;

/**
 * Bridge between xarch-example's business layer and the new
 * {@link FileStorageService} from xarch-storage-spring-boot-starter.
 * <p>
 * Wraps the generic storage result into a {@link Resource} entity and
 * adds scene-code based key generation.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileStorageService fileStorageService;

    /**
     * Upload a multipart file using the supplied scene and storage type.
     *
     * @param sceneCode   logical scene code (e.g. "avatar", "document")
     * @param bizKey      optional business key
     * @param storageType storage backend code (case-insensitive)
     * @param file        the multipart file
     * @param userId      uploading user id (may be null)
     * @param userName    uploading user name (may be null)
     * @return a persisted {@link Resource} entity
     * @throws StorageException on upload failure
     */
    public Resource upload(String sceneCode, String bizKey, String storageType,
                            MultipartFile file, Long userId, String userName) {
        if (file == null || file.isEmpty()) {
            throw new StorageException("Cannot upload empty file");
        }
        UploadOptions options = UploadOptions.builder()
                .storageType(StorageType.fromCode(storageType))
                .objectKeyPrefix(prefixFor(sceneCode))
                .contentType(file.getContentType())
                .originalFilename(file.getOriginalFilename())
                .build();
        StorageResult result = fileStorageService.upload(file, options);
        log.info("Uploaded file: scene={} bizKey={} type={} key={} size={}",
                sceneCode, bizKey, storageType, result.objectKey(), result.size());
        Resource resource = new Resource();
        resource.setResourceName(file.getOriginalFilename());
        resource.setObjectKey(result.objectKey());
        resource.setAccessUrl(result.accessUrl());
        resource.setSceneCode(sceneCode);
        resource.setBizKey(bizKey);
        resource.setFileSize(result.size());
        resource.setFileType(result.contentType());
        resource.setStorageType(StorageType.fromCode(storageType).getCode());
        resource.setCreateUserId(userId);
        resource.setCreateUserName(userName);
        resource.setDelFlag(0);
        return resource;
    }

    /**
     * Open an input stream for downloading the given resource.
     *
     * @param resource the resource to stream
     * @return a non-null {@link InputStream}
     */
    public InputStream openStream(Resource resource) {
        StorageType type = StorageType.fromCode(resource.getStorageType());
        return fileStorageService.download(type, null, resource.getObjectKey());
    }

    /**
     * Delete the underlying object for the given resource.
     *
     * @param resource the resource to delete
     */
    public void delete(Resource resource) {
        StorageType type = StorageType.fromCode(resource.getStorageType());
        fileStorageService.delete(type, null, resource.getObjectKey());
    }

    /**
     * Generate a time-limited presigned download URL.
     *
     * @param resource  the resource
     * @param expiry    how long the URL should remain valid
     * @return a presigned URL
     */
    public String presignedUrl(Resource resource, Duration expiry) {
        StorageType type = StorageType.fromCode(resource.getStorageType());
        return fileStorageService.presignedUrl(type, null, resource.getObjectKey(), expiry);
    }

    /**
     * Translate a scene code into a stable key prefix.
     *
     * @param sceneCode scene code, may be null
     * @return object key prefix, never null
     */
    private String prefixFor(String sceneCode) {
        if (sceneCode == null || sceneCode.isBlank()) {
            return "misc/";
        }
        return sceneCode + "/";
    }
}
