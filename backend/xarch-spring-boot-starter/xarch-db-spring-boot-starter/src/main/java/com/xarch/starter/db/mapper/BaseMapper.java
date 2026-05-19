package com.xarch.starter.db.mapper;

/**
 * Generic base mapper for MybatisFlex
 * @param <T> entity type
 */
public interface BaseMapper<T> extends com.mybatisflex.core.BaseMapper<T> {

    /**
     * Insert entity and get generated ID
     */
    default int insertAndGetId(T entity) {
        return insert(entity);
    }
}