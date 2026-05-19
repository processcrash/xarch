package com.xarch.starter.db.mapper;

import com.mybatis.flex.core.mapper.BaseMapper;

/**
 * Generic base mapper for MybatisFlex
 * @param <T> entity type
 */
public interface BaseMapper<T> extends com.mybatis.flex.core.mapper.BaseMapper<T> {

    /**
     * Insert entity and get generated ID
     */
    default int insertAndGetId(T entity) {
        return insert(entity);
    }
}