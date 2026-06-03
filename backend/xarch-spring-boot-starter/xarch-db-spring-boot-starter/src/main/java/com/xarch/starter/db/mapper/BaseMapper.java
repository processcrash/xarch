package com.xarch.starter.db.mapper;

import com.mybatisflex.core.query.QueryWrapper;

import java.util.List;

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

    /**
     * Select entity by ID (convenience method for selectOneById)
     */
    default T selectById(Long id) {
        return selectOneById(id);
    }

    /**
     * Update entity by ID (convenience method for updateByQuery)
     */
    default int updateById(T entity) {
        return update(entity);
    }

    /**
     * Select all entities (no filter)
     */
    default List<T> selectList() {
        return selectListByQuery(QueryWrapper.create());
    }
}