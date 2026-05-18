package com.xarch.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xarch.example.entity.DictData;
import org.apache.ibatis.annotations.Mapper;

/**
 * Dictionary data mapper
 */
@Mapper
public interface DictDataMapper extends BaseMapper<DictData> {
}