package com.xarch.example.mapper;

import com.xarch.example.entity.Config;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Config mapper
 */
@Mapper
public interface ConfigMapper extends BaseMapper<Config> {
}