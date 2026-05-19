package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Column;
import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Operation log entity
 */
@Data
@Table("sys_op_log")
public class OpLog implements Serializable {

    @Id(auto = true)
    private Long id;

    private String username;

    private String operation;

    private String type;

    private String method;

    private String ip;

    private String location;

    private String params;

    private String result;

    private Integer status;

    private Long costTime;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;
}