package com.xarch.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xarch.example.entity.OpLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * Operation log mapper
 */
@Mapper
public interface OpLogMapper extends BaseMapper<OpLog> {
}