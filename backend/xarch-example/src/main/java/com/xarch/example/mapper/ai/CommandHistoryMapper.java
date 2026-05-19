package com.xarch.example.mapper.ai;

import com.xarch.starter.db.mapper.BaseMapper;
import com.xarch.example.entity.ai.CommandHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * Command history mapper
 */
@Mapper
public interface CommandHistoryMapper extends BaseMapper<CommandHistory> {
}