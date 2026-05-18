package com.xarch.example.controller;

import com.xarch.example.excel.UserExcel;
import com.xarch.example.service.ExcelService;
import com.xarch.starter.core.result.ApiResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExcelController unit tests
 */
@SpringBootTest
class ExcelControllerTest {

    @Autowired
    private ExcelController excelController;

    @Autowired
    private ExcelService excelService;

    @Test
    void testExportUsers() {
        List<UserExcel> users = excelService.exportUsers();
        assertNotNull(users);
    }

    @Test
    void testImportUsersWithInvalidFile() {
        // Empty file should fail
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );
        ApiResult<Integer> result = excelController.importUsers(emptyFile);
        assertNotNull(result);
        // Should return error for empty file
        assertEquals("1001", result.getCode());
    }
}