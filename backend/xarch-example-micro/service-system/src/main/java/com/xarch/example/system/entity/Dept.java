package com.xarch.example.system.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Department entity owned by {@code service-system}.
 */
@Data
@Table("xarch_system_dept")
public class Dept implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    private Long parentId;

    private String deptName;

    private String deptCode;

    private Integer sortOrder;

    private String leader;

    private String phone;

    private Integer status;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;

    private transient java.util.List<Dept> children;
}