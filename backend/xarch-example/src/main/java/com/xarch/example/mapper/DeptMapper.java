package com.xarch.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xarch.example.entity.Dept;
import org.apache.ibatis.annotations.Mapper;

/**
 * Department mapper
 */
@Mapper
public interface DeptMapper extends BaseMapper<Dept> {
}