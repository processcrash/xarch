package com.xarch.starter.core.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Login user info stored in session
 */
@Data
@Schema(description = "Login user info")
public class LoginUser implements BaseUserInfo {

    @Schema(description = "User type: 1=admin, 2=normal")
    private Integer userType = 2;

    @Schema(description = "Role IDs")
    private String roleIds;

    @Schema(description = "Role names")
    private String roleNames;

    @Schema(description = "Permission codes")
    private String permissions;
}