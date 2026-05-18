package com.xarch.example.controller;

import com.xarch.example.excel.UserExcel;
import com.xarch.example.service.ExcelService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel import/export controller
 */
@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    @Autowired
    private ExcelService excelService;

    /**
     * Export users to Excel
     */
    @GetMapping("/export/users")
    @XarchLog(value = "Export users to Excel", type = "EXPORT")
    public void exportUsers(HttpServletResponse response) {
        try {
            List<UserExcel> users = excelService.exportUsers();

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("user_export", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            com.alibaba.excel.EasyExcel.write(response.getOutputStream(), UserExcel.class)
                    .sheet("Users")
                    .doWrite(users);
        } catch (IOException e) {
            throw new RuntimeException("Failed to export users", e);
        }
    }

    /**
     * Import users from Excel
     */
    @PostMapping("/import/users")
    @XarchLog(value = "Import users from Excel", type = "IMPORT")
    public ApiResult<Integer> importUsers(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResult.fail("1001", "File is empty");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ApiResult.fail("1001", "File must be Excel format (.xlsx or .xls)");
        }
        int count = excelService.importUsers(file);
        return ApiResult.ok(count);
    }
}