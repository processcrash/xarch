package com.xarch.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.Config;
import com.xarch.example.mapper.ConfigMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Config service
 */
@Service
public class ConfigService {

    @Autowired
    private ConfigMapper configMapper;

    public PageResult<Config> page(String configKey, int pageNum, int pageSize) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Config>();
        if (configKey != null && !configKey.isEmpty()) {
            wrapper.like(Config::getConfigKey, configKey);
        }
        wrapper.orderByDesc(Config::getCreateTime);

        Page<Config> page = new Page<>(pageNum, pageSize);
        Page<Config> result = configMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    public Config getById(Long id) {
        return configMapper.selectById(id);
    }

    public String getValue(String configKey) {
        Config config = configMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Config>()
                .eq(Config::getConfigKey, configKey)
        ).stream().findFirst().orElse(null);
        return config != null ? config.getConfigValue() : null;
    }

    public void create(Config config) {
        configMapper.insert(config);
    }

    public void update(Config config) {
        configMapper.updateById(config);
    }

    public void delete(Long id) {
        configMapper.deleteById(id);
    }
}