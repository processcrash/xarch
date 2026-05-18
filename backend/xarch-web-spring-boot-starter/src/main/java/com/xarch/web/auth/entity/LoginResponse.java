package com.xarch.web.auth.entity;

import java.io.Serializable;

/**
 * Login response with token
 */
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;
    private Long expireTime;
    private String username;
    private String roles;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long expireTime, String username, String roles) {
        this.token = token;
        this.expireTime = expireTime;
        this.username = username;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }
}