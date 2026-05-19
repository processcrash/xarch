package com.xarch.example.mapper;

import com.xarch.example.entity.Dept;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Department mapper
 */
@Mapper
public interface DeptMapper extends BaseMapper<Dept> {
}