package com.xarch.example.file.controller;

import com.xarch.example.file.entity.Resource;
import com.xarch.example.file.service.ResourceService;
import com.xarch.starter.core.annotation.Debounce;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Resource controller — migrated from monolith. */
@Tag(name = "Resource Management")
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @GetMapping
    @XarchLog(value = "Query resource list", type = "QUERY")
    public ApiResult<PageResult<Resource>> page(
            @RequestParam(required = false) String sceneCode,
            @RequestParam(required = false) String storageType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(resourceService.page(sceneCode, storageType, keyword, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<Resource> detail(@PathVariable Long id) {
        return ApiResult.ok(resourceService.getById(id));
    }

    @PostMapping("/upload")
    @Debounce
    @XarchLog(value = "Upload resource", type = "CREATE")
    public ApiResult<Map<String, Object>> upload(
            @RequestParam(required = false) String sceneCode,
            @RequestParam(required = false) String bizKey,
            @RequestParam(required = false) String storageType,
            @RequestPart MultipartFile file) throws IOException {
        Resource resource = resourceService.upload(sceneCode, bizKey, storageType, file, null, null);
        Map<String, Object> result = new HashMap<>();
        result.put("objectKey", resource.getObjectKey());
        result.put("accessUrl", resource.getAccessUrl());
        result.put("resourceId", resource.getId());
        result.put("fileName", resource.getResourceName());
        result.put("fileSize", resource.getFileSize());
        return ApiResult.ok(result);
    }

    @PostMapping("/batchUpload")
    @Debounce
    @XarchLog(value = "Batch upload resources", type = "CREATE")
    public ApiResult<List<Map<String, Object>>> batchUpload(
            @RequestParam(required = false) String sceneCode,
            @RequestParam(required = false) String storageType,
            @RequestPart List<MultipartFile> files) throws IOException {
        List<Map<String, Object>> results = new ArrayList<>();
        for (MultipartFile file : files) {
            Resource resource = resourceService.upload(sceneCode, null, storageType, file, null, null);
            Map<String, Object> item = new HashMap<>();
            item.put("objectKey", resource.getObjectKey());
            item.put("accessUrl", resource.getAccessUrl());
            item.put("resourceId", resource.getId());
            item.put("fileName", resource.getResourceName());
            item.put("fileSize", resource.getFileSize());
            results.add(item);
        }
        return ApiResult.ok(results);
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete resource", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        resourceService.delete(id);
        return ApiResult.ok();
    }

    @GetMapping("/options")
    public ApiResult<List<Resource>> options() {
        return ApiResult.ok(resourceService.list());
    }
}