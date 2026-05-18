package com.xarch.starter.db.mapper;

import java.util.List;

/**
 * Generic base mapper for MyBatis Plus
 * @param <T> entity type
 */
public interface BaseMapper<T> {

    int insert(T entity);

    int updateById(T entity);

    int deleteById(Long id);

    T selectById(Long id);

    List<T> selectList();
}