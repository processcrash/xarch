package com.xarch.starter.core.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * Base user info
 */
@Data
@Schema(description = "Base user info")
public class BaseUserInfo implements Serializable {

    @Schema(description = "User ID")
    private Long userId;

    @Schema(description = "Username")
    private String username;

    @Schema(description = "Nickname")
    private String nickname;

    @Schema(description = "Department ID")
    private Long deptId;

    @Schema(description = "Department name")
    private String deptName;
}