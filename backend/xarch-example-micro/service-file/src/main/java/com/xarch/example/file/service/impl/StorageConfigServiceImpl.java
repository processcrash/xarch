package com.xarch.example.file.service.impl;

import com.xarch.example.file.entity.StorageConfig;
import com.xarch.example.file.service.StorageConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** Stub StorageConfigService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageConfigServiceImpl implements StorageConfigService {
    @Override public List<StorageConfig> listEnabled() { return List.of(); }
    @Override public StorageConfig getById(Long id) { return null; }
    @Override public void create(StorageConfig c) { }
    @Override public void update(StorageConfig c) { }
    @Override public void delete(Long id) { }
    @Override public boolean testConnection(StorageConfig c) { return false; }
}