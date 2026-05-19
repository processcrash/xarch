package com.xarch.example.entity;

import com.mybatis.flex.core.annotation.Column;
import com.mybatis.flex.core.annotation.Id;
import com.mybatis.flex.core.annotation.Table;
import com.mybatis.flex.core.annotation.Version;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User entity
 */
@Data
@Table("sys_user")
public class User implements Serializable {

    @Id(auto = true)
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