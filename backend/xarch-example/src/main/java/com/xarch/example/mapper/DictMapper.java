package com.xarch.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xarch.example.entity.Dict;
import org.apache.ibatis.annotations.Mapper;

/**
 * Dictionary mapper
 */
@Mapper
public interface DictMapper extends BaseMapper<Dict> {
}