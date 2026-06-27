package com.xarch.example.file.service;

import com.xarch.example.file.entity.TempFile;
import com.xarch.starter.core.result.PageResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** TempFile service contract. */
public interface TempFileService {
    PageResult<TempFile> page(String fileName, int pageNum, int pageSize);
    TempFile getById(Long id);
    TempFile uploadFile(MultipartFile file) throws IOException;
    void create(TempFile tempFile);
    void update(TempFile tempFile);
    void delete(Long id);
}