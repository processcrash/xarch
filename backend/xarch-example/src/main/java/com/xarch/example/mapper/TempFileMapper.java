package com.xarch.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xarch.example.entity.TempFile;
import org.apache.ibatis.annotations.Mapper;

/**
 * Temp file mapper
 */
@Mapper
public interface TempFileMapper extends BaseMapper<TempFile> {
}