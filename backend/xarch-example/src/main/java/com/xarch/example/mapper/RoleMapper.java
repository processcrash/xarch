package com.xarch.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xarch.example.entity.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * Role mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}