package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Column;
import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Message entity
 */
@Data
@Table("sys_message")
public class Message implements Serializable {

    @Id(auto = true)
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

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}