package com.xarch.example.mapper;

import com.xarch.example.entity.DictData;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Dictionary data mapper
 */
@Mapper
public interface DictDataMapper extends BaseMapper<DictData> {
}