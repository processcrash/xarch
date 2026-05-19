package com.xarch.example.mapper.ai;

import com.xarch.starter.db.mapper.BaseMapper;
import com.xarch.example.entity.ai.CommandAudit;
import org.apache.ibatis.annotations.Mapper;

/**
 * Command Audit Mapper
 */
@Mapper
public interface CommandAuditMapper extends BaseMapper<CommandAudit> {
}