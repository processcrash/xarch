package com.xarch.db.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Generic base mapper interface
 *
 * @param <T> entity type
 * @param <P> primary key type
 */
public interface BaseMapper<T, P> {

    /**
     * Insert entity
     */
    int insert(@Param("entity") T entity);

    /**
     * Insert entity with auto-generate id
     */
    int insertSelective(@Param("entity") T entity);

    /**
     * Update by primary key
     */
    int updateByPrimaryKey(@Param("entity") T entity);

    /**
     * Update by primary key selectively
     */
    int updateByPrimaryKeySelective(@Param("entity") T entity);

    /**
     * Delete by primary key
     */
    int deleteByPrimaryKey(@Param("id") P id);

    /**
     * Select by primary key
     */
    T selectByPrimaryKey(@Param("id") P id);

    /**
     * Select all
     */
    List<T> selectAll();

    /**
     * Select by condition
     */
    List<T> selectByCondition(@Param("condition") T condition);

    /**
     * Count by condition
     */
    long countByCondition(@Param("condition") T condition);
}