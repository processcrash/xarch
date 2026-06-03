package com.xarch.example.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 参数配置表 sys_config
 */
@Data
@Table("sys_config")
public class SysConfig {

    /** 参数主键 */
    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long configId;

    /** 参数名称 */
    private String configName;

    /** 参数键名 */
    private String configKey;

    /** 参数键值 */
    private String configValue;

    /** 系统内置（Y是 N否） */
    private String configType = "N";
}