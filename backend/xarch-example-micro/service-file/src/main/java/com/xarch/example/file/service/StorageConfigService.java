package com.xarch.example.file.service;

import com.xarch.example.file.entity.StorageConfig;

import java.util.List;

/** Storage configuration service contract. */
public interface StorageConfigService {
    List<StorageConfig> listEnabled();
    StorageConfig getById(Long id);
    void create(StorageConfig config);
    void update(StorageConfig config);
    void delete(Long id);
    boolean testConnection(StorageConfig config);
}