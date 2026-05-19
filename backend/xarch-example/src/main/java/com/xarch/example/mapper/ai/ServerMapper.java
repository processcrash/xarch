package com.xarch.example.mapper.ai;

import com.xarch.starter.db.mapper.BaseMapper;
import com.xarch.example.entity.ai.Server;
import org.apache.ibatis.annotations.Mapper;

/**
 * Server mapper
 */
@Mapper
public interface ServerMapper extends BaseMapper<Server> {
}