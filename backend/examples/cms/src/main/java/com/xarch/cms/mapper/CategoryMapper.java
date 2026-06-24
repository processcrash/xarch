package com.xarch.cms.mapper;

import com.xarch.cms.entity.Category;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Category mapper. Tree assembly is done in the service layer.
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
