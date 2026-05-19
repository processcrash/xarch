package com.xarch.example.mapper;

import com.xarch.starter.db.mapper.BaseMapper;
import com.xarch.example.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * Message mapper
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}