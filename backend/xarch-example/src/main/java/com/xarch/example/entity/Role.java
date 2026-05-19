package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Column;
import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Role entity
 */
@Data
@Table("sys_role")
public class Role implements Serializable {

    @Id(auto = true)
    private Long id;

    private String roleName;

    private String roleCode;

    private Integer roleType;

    private String description;

    private Integer status;

    /**
     * Menu IDs assigned to this role (comma-separated for transfer component)
     */
    private String menuIds;

    /**
     * Department IDs for data permission (comma-separated)
     */
    private String deptIds;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}