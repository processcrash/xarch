package com.xarch.example.service;

import com.xarch.starter.core.result.ApiResult;
import org.springframework.stereotype.Service;

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