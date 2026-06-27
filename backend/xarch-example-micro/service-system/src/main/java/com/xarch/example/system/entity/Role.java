package com.xarch.example.system.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Role entity owned by {@code service-system}.
 */
@Data
@Table("xarch_system_role")
public class Role implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    private String roleName;

    private String roleCode;

    private Integer roleType;

    private String description;

    private Integer status;

    /** Menu IDs assigned to this role (comma-separated for transfer component). */
    private String menuIds;

    /** Department IDs for data permission (comma-separated). */
    private String deptIds;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}