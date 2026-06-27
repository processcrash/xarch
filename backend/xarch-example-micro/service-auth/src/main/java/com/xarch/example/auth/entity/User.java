package com.xarch.example.auth.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User entity owned by {@code service-auth}.
 *
 * <p>Table name uses the {@code xarch_auth_} prefix to keep schema
 * ownership obvious when many services share a single MySQL instance.
 */
@Data
@Table("xarch_auth_user")
public class User implements Serializable {

    @Id(keyType = com.mybatisflex.annotation.KeyType.Auto)
    private Long id;

    @Column(onInsertValue = "NOW()")
    private String username;

    private String password;

    private String nickname;

    private String email;

    private String mobile;

    private Integer status;

    private Long deptId;

    private Integer userType;

    private String roleIds;

    @Column(onInsertValue = "NOW()")
    private LocalDateTime createTime;

    @Column(onUpdateValue = "NOW()")
    private LocalDateTime updateTime;

    private Integer delFlag;
}