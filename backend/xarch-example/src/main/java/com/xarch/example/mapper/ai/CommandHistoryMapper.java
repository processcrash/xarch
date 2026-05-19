package com.xarch.example.mapper.ai;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xarch.example.entity.ai.CommandHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * Command history mapper
 */
@Mapper
public interface CommandHistoryMapper extends BaseMapper<CommandHistory> {
}