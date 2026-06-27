package com.xarch.example.system.service;

import com.xarch.example.system.entity.Config;
import com.xarch.starter.core.result.PageResult;

/** Config service contract. */
public interface ConfigService {
    PageResult<Config> page(String configKey, int pageNum, int pageSize);
    Config getById(Long id);
    String getValue(String configKey);
    void create(Config config);
    void update(Config config);
    void delete(Long id);
}