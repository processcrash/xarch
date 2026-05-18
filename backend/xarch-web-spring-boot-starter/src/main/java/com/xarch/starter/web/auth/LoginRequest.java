package com.xarch.starter.web.auth;

import lombok.Data;
import java.io.Serializable;

/**
 * Login request
 */
@Data
public class LoginRequest implements Serializable {
    private String username;
    private String password;
    private String captcha;
    private String captchaKey;
}