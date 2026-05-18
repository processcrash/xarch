package com.xarch.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xarch.example.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * User mapper using MyBatis Plus
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}