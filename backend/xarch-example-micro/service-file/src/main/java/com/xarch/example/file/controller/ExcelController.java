package com.xarch.example.file.controller;

import com.xarch.example.file.service.ExcelService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Excel import/export controller.
 *
 * <p>Implementation will use Alibaba EasyExcel once user DTOs land in
 * this service. Until then the controller returns zero rows but exposes
 * the same path shape.
 */
@Tag(name = "Excel Import/Export")
@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;

    /**
     * Export users as an Excel file. Placeholder — writes an empty workbook.
     */
    @GetMapping("/export/users")
    @XarchLog(value = "Export users to Excel", type = "EXPORT")
    public void exportUsers(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''user_export.xlsx");
            // Concrete write of EasyExcel rows will be added with the user DTO.
        } catch (Exception e) {
            throw new RuntimeException("Failed to export users", e);
        }
    }

    /**
     * Import users from an Excel file.
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
        return ApiResult.ok(excelService.importUsers(file));
    }
}