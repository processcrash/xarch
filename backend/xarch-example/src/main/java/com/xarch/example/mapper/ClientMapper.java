package com.xarch.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xarch.example.entity.Client;
import org.apache.ibatis.annotations.Mapper;

/**
 * Client mapper
 */
@Mapper
public interface ClientMapper extends BaseMapper<Client> {
}