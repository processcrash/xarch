package com.xarch.example.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Message entity
 */
@Data
@TableName("sys_message")
public class Message implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private Integer msgType;

    private Integer category;

    private Long senderId;

    private String senderName;

    private Integer isRead;

    private Integer priority;

    private String extraData;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private Integer delFlag;
}