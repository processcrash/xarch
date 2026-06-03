package com.xarch.example.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Operation log entity
 */
@Data
@Table("sys_op_log")
public class OpLog implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
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