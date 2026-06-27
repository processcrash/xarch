package com.xarch.example.file.service.impl;

import com.xarch.example.file.entity.Resource;
import com.xarch.example.file.service.ResourceService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/** Stub ResourceService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    @Override public PageResult<Resource> page(String sc, String st, String kw, int p, int s) { return PageResult.empty(); }
    @Override public Resource getById(Long id) { return null; }
    @Override public Resource upload(String sc, String bk, String st, MultipartFile f, Long uid, String un) throws IOException { return null; }
    @Override public boolean download(Long id, OutputStream os) { return false; }
    @Override public InputStream getFileStream(Long id) { return null; }
    @Override public void delete(Long id) { }
    @Override public List<Resource> list() { return List.of(); }
    @Override public StorageStats getStats() { return new StorageStats(); }
}