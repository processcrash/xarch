package com.xarch.example.mapper;

import com.xarch.starter.db.mapper.BaseMapper;
import com.xarch.example.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * Login log mapper
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}