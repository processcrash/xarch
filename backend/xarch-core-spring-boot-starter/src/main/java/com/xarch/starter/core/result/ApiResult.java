package com.xarch.starter.core.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * Unified API response
 */
@Data
@Schema(description = "API response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> implements Serializable {

    @Schema(description = "Response code: 0000=success, other=error")
    private String code = "0000";

    @Schema(description = "Response message")
    private String message = "SUCCESS";

    @Schema(description = "Response data")
    private T data;

    @Schema(description = "Timestamp")
    private Long timestamp = System.currentTimeMillis();

    public ApiResult() {
    }

    public ApiResult(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiResult(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResult<T> ok() {
        return new ApiResult<>("0000", "SUCCESS");
    }

    public static <T> ApiResult<T> ok(T data) {
        return new ApiResult<>("0000", "SUCCESS", data);
    }

    public static <T> ApiResult<T> fail(String code, String message) {
        return new ApiResult<>(code, message);
    }

    public static <T> ApiResult<T> fail(String code, String message, T data) {
        return new ApiResult<>(code, message, data);
    }
}