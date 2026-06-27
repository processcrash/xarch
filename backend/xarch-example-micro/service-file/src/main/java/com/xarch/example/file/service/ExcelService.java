package com.xarch.example.file.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Excel import/export service contract. */
public interface ExcelService {
    List<?> exportUsers();
    int importUsers(MultipartFile file);
}