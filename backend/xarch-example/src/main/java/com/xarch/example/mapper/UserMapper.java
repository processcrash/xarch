package com.xarch.example.mapper;

import com.xarch.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * User mapper
 */
@Mapper
public interface UserMapper {

    User findById(@Param("id") Long id);

    User findByUsername(@Param("username") String username);

    List<User> findAll();

    int insert(User user);

    int update(User user);

    int deleteById(@Param("id") Long id);
}