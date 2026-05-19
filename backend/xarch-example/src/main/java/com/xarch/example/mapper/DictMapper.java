package com.xarch.example.mapper;

import com.xarch.example.entity.Dict;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Dictionary mapper
 */
@Mapper
public interface DictMapper extends BaseMapper<Dict> {
}