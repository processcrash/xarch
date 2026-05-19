package com.xarch.example.mapper;

import com.xarch.example.entity.Menu;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Menu mapper
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {
}