package com.xarch.example.service;

import com.xarch.example.entity.User;
import com.xarch.example.excel.UserExcel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Excel import/export service interface
 */
public interface ExcelService {

    /**
     * Export users to Excel file
     * @return List of UserExcel objects
     */
    List<UserExcel> exportUsers();

    /**
     * Import users from Excel file
     * @param file Excel file
     * @return number of imported users
     */
    int importUsers(MultipartFile file);
}