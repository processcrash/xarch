package com.xarch.example.file.service.impl;

import com.xarch.example.file.service.ExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Stub ExcelService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {
    @Override public List<?> exportUsers() { return List.of(); }
    @Override public int importUsers(MultipartFile file) { return 0; }
}