package com.xarch.example.file.client;

import com.xarch.starter.core.result.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Feign client for the file-upload endpoint exposed by {@code service-file}.
 *
 * <p>Consumed by peer services that need to upload files (e.g. service-ai
 * attaching key material, service-message attaching attachments).
 */
@FeignClient(name = "xarch-service-file", path = "/file")
public interface FileFeignClient {

    /**
     * Upload a file via multipart form.
     *
     * @param file multipart file part
     * @return envelope with file metadata
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResult<Map<String, Object>> upload(@RequestPart("file") MultipartFile file);
}