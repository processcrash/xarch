package com.xarch.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xarch.example.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * Login log mapper
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}