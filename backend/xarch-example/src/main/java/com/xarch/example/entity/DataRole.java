package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Column;
import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Data role entity for row-level permissions
 */
@Data
@Table("sys_data_role")
public class DataRole implements Serializable {

    @Id(auto = true)
    private Long id;

    private String roleName;

    private String roleCode;

    private String dataScope;

    private String description;

    private Integer status;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}