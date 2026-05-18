package com.xarch.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xarch.example.entity.Menu;
import org.apache.ibatis.annotations.Mapper;

/**
 * Menu mapper
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {
}