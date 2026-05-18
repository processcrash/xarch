package com.xarch.example.controller;

import com.xarch.example.service.CommonService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.annotation.Debounce;
import com.xarch.starter.core.result.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Common controller for shared operations
 */
@RestController
@RequestMapping("/api/common")
public class CommonController {

    @Autowired
    private CommonService commonService;

    @GetMapping("/selector")
    public ApiResult<Map<String, Object>> querySelector(
            @RequestParam String type,
            @RequestParam(required = false) String keyword) {
        return ApiResult.ok(commonService.querySelector(type, keyword));
    }

    @GetMapping("/auth/challenge")
    @Debounce
    @XarchLog(value = "Get auth challenge", type = "QUERY")
    public ApiResult<Map<String, Object>> challenge() {
        return ApiResult.ok(commonService.challenge());
    }

    @GetMapping("/oss/private-url")
    public ApiResult<String> getOssPrivateUrl(
            @RequestParam(required = false) String bucket,
            @RequestParam String url) {
        return ApiResult.ok(commonService.ossPrivateUrl(bucket, url));
    }

    @PostMapping("/files/download")
    @XarchLog(value = "Download file", type = "DOWNLOAD")
    public void download(@RequestBody Map<String, String> params, HttpServletResponse response) throws IOException {
        String url = params.get("url");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=download");
        commonService.urlDownload(url, response.getOutputStream());
    }

    @GetMapping("/download/templates")
    public void templateDownload(
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String alias,
            HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=" + (templateName != null ? templateName : "template"));
        commonService.tempDownload(templateName, alias, response.getOutputStream());
    }
}