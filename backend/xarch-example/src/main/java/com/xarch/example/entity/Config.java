package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Column;
import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * System config entity
 */
@Data
@Table("sys_config")
public class Config implements Serializable {

    @Id(auto = true)
    private Long id;

    private String configKey;

    private String configValue;

    private String configType;

    private String description;

    private Integer status;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}