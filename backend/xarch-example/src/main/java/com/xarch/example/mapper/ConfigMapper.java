package com.xarch.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xarch.example.entity.Config;
import org.apache.ibatis.annotations.Mapper;

/**
 * Config mapper
 */
@Mapper
public interface ConfigMapper extends BaseMapper<Config> {
}