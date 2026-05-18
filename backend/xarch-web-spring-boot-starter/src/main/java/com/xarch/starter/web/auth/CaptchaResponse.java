package com.xarch.starter.web.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;

/**
 * Captcha response
 */
@Data
@AllArgsConstructor
public class CaptchaResponse implements Serializable {
    private String captchaBase64;
    private String captchaKey;
}