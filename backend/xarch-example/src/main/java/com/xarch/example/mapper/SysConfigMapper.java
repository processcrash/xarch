package com.xarch.example.mapper;

import com.xarch.starter.db.mapper.BaseMapper;
import com.xarch.example.entity.SysConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 参数配置 数据层
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
    /**
     * 查询参数配置信息
     */
    SysConfig selectConfig(SysConfig config);

    /**
     * 通过ID查询配置
     */
    SysConfig selectConfigById(Long configId);

    /**
     * 查询参数配置列表
     */
    List<SysConfig> selectConfigList(SysConfig config);

    /**
     * 根据键名查询参数配置信息
     */
    SysConfig checkConfigKeyUnique(String configKey);

    /**
     * 新增参数配置
     */
    int insertConfig(SysConfig config);

    /**
     * 修改参数配置
     */
    int updateConfig(SysConfig config);

    /**
     * 删除参数配置
     */
    int deleteConfigById(Long configId);

    /**
     * 批量删除参数信息
     */
    int deleteConfigByIds(Long[] configIds);
}