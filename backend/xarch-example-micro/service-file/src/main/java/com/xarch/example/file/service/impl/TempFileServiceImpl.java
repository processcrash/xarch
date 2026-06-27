package com.xarch.example.file.service.impl;

import com.xarch.example.file.entity.TempFile;
import com.xarch.example.file.service.TempFileService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/** Stub TempFileService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class TempFileServiceImpl implements TempFileService {
    @Override public PageResult<TempFile> page(String n, int p, int s) { return PageResult.empty(); }
    @Override public TempFile getById(Long id) { return null; }
    @Override public TempFile uploadFile(MultipartFile f) throws IOException { return null; }
    @Override public void create(TempFile t) { }
    @Override public void update(TempFile t) { }
    @Override public void delete(Long id) { }
}