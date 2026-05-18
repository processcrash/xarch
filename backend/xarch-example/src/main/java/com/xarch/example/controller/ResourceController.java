package com.xarch.example.controller;

import com.xarch.example.entity.Resource;
import com.xarch.example.service.ResourceService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.annotation.Debounce;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource controller for file management
 */
@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @GetMapping
    @XarchLog(value = "Query resource list", type = "QUERY")
    public ApiResult<PageResult<Resource>> page(
            @RequestParam(required = false) String sceneCode,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(resourceService.page(sceneCode, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<Resource> detail(@PathVariable Long id) {
        return ApiResult.ok(resourceService.getById(id));
    }

    @PostMapping("/upload")
    @Debounce
    @XarchLog(value = "Upload resource", type = "CREATE")
    public ApiResult<Map<String, Object>> upload(
            @RequestParam String sceneCode,
            @RequestParam(required = false) String bizKey,
            @RequestParam(required = false) String pathSegments,
            @RequestPart MultipartFile file) throws IOException {

        String[] segments = pathSegments != null && !pathSegments.isBlank() ? pathSegments.split(",") : new String[0];
        Resource resource = resourceService.upload(sceneCode, bizKey, file, segments);

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
            @RequestParam String sceneCode,
            @RequestParam(required = false) String pathSegments,
            @RequestPart List<MultipartFile> files) throws IOException {

        String[] segments = pathSegments != null && !pathSegments.isBlank() ? pathSegments.split(",") : new String[0];
        List<Map<String, Object>> results = new java.util.ArrayList<>();

        for (MultipartFile file : files) {
            Resource resource = resourceService.upload(sceneCode, null, file, segments);
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

    @PostMapping
    @XarchLog(value = "Create resource", type = "CREATE")
    public ApiResult<Void> create(@RequestBody Resource resource) {
        resourceService.create(resource);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update resource", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Resource resource) {
        resource.setId(id);
        resourceService.update(resource);
        return ApiResult.ok();
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