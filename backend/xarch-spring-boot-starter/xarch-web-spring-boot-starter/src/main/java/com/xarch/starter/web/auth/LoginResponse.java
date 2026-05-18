package com.xarch.starter.web.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

/**
 * Login response with token
 */
@Data
@AllArgsConstructor
public class LoginResponse implements Serializable {
    private String token;
    private Long expireTime;
    private String username;
    private String roles;
    private String nickname;
}