package com.xarch.example.common.response;

/**
 * Uniform response envelope used across all micro-services.
 *
 * <p>This intentionally mirrors {@code com.xarch.starter.core.result.ApiResult}
 * but lives in the shared {@code common} module so Feign fallbacks can
 * return it without depending on the starter at compile-time.
 *
 * @param <T> payload type
 * @param code      numeric status code (HTTP-style)
 * @param message   human-readable status message
 * @param data      payload — may be {@code null} for void operations
 */
public record MicroResponse<T>(int code, String message, T data) {

    /**
     * Build a success response with the given payload.
     *
     * @param data payload (may be {@code null})
     * @param <T>  payload type
     * @return success envelope with code=200
     */
    public static <T> MicroResponse<T> ok(T data) {
        return new MicroResponse<>(200, "OK", data);
    }

    /**
     * Build an empty success response.
     *
     * @param <T> payload type
     * @return success envelope with code=200 and {@code data=null}
     */
    public static <T> MicroResponse<T> ok() {
        return new MicroResponse<>(200, "OK", null);
    }

    /**
     * Build a failure response.
     *
     * @param code    numeric error code
     * @param message human-readable error message
     * @param <T>     payload type
     * @return failure envelope
     */
    public static <T> MicroResponse<T> fail(int code, String message) {
        return new MicroResponse<>(code, message, null);
    }
}