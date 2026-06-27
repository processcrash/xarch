package com.xarch.example.message.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Message entity owned by {@code service-message}. */
@Data
@Table("xarch_message_message")
public class Message implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
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