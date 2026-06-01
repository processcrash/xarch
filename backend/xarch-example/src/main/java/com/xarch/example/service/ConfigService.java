package com.xarch.example.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.entity.Config;
import com.xarch.example.mapper.ConfigMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Config service
 */
@Service
public class ConfigService {

    @Autowired
    private ConfigMapper configMapper;

    public PageResult<Config> page(String configKey, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_config").where("del_flag = 0");
        if (StringUtils.hasText(configKey)) {
            wrapper.and("config_key LIKE ?", "%" + configKey + "%");
        }
        wrapper.orderBy("create_time", false);

        Page<Config> page = configMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    public Config getById(Long id) {
        return configMapper.selectById(id);
    }

    public String getValue(String configKey) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_config")
                .where("config_key = ?", configKey)
                .limit(1);
        Config config = configMapper.selectOneByQuery(wrapper);
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