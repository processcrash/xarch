package com.xarch.example.service;

import com.xarch.starter.core.result.ApiResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Common service for shared operations
 */
@Service
public class CommonService {

    private static final String TEMPLATE_DIR = "/tmp/xarch-templates/";
    private static final String UPLOAD_DIR = "/tmp/xarch-uploads/";

    public Map<String, Object> querySelector(String type, String keyword) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", type);
        result.put("keyword", keyword);
        result.put("items", List.of());
        return result;
    }

    public Map<String, Object> challenge() {
        Map<String, Object> result = new HashMap<>();
        result.put("seed", UUID.randomUUID().toString());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    public String ossPrivateUrl(String bucket, String url) {
        return url;
    }

    public void urlDownload(String url, OutputStream outputStream) throws IOException {
    }

    public String uploadFile(MultipartFile file) throws IOException {
        // Create upload directory if not exists
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath);
        // Return URL path (in real scenario, this would be OSS/S3 URL)
        return "/api/common/files/download?url=" + newFilename;
    }

    public void tempDownload(String templateName, String alias, OutputStream outputStream) throws IOException {
        if (templateName == null && alias != null) {
            templateName = alias;
        }
        if (templateName == null) {
            templateName = "default";
        }

        Path templatePath = Paths.get(TEMPLATE_DIR + templateName);
        if (Files.exists(templatePath)) {
            Files.copy(templatePath, outputStream);
        } else {
            String defaultTemplate = "Template content for " + templateName;
            outputStream.write(defaultTemplate.getBytes());
        }
        outputStream.flush();
    }
}