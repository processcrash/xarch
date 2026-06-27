package com.xarch.example.ai.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/** User behaviour entity — used by {@code UserBehaviorController}. */
@Data
@Table("xarch_ai_user_behavior")
public class UserBehavior {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    private Long userId;

    private String userName;

    private String action;

    private String target;

    private String ip;

    private String userAgent;

    private String extraData;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;
}