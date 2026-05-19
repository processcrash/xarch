package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Column;
import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Department entity
 */
@Data
@Table("sys_dept")
public class Dept implements Serializable {

    @Id(auto = true)
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