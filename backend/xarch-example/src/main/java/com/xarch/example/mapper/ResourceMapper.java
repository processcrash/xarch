package com.xarch.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xarch.example.entity.Resource;
import org.apache.ibatis.annotations.Mapper;

/**
 * Resource mapper
 */
@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {
}