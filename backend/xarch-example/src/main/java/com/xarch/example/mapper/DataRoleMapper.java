package com.xarch.example.mapper;

import com.xarch.starter.db.mapper.BaseMapper;
import com.xarch.example.entity.DataRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * Data role mapper
 */
@Mapper
public interface DataRoleMapper extends BaseMapper<DataRole> {
}