package com.xarch.example.common.dto;

/**
 * Standard HTTP-style error codes used by every micro-service.
 *
 * <p>The numeric value matches the HTTP status code so callers can map
 * directly to REST semantics without a translation table.
 */
public enum ErrorCode {

    /** Operation succeeded. */
    OK(200, "OK"),

    /** Client request was malformed (missing fields, invalid types, etc.). */
    BAD_REQUEST(400, "Bad Request"),

    /** Caller is not authenticated. */
    UNAUTHORIZED(401, "Unauthorized"),

    /** Caller is authenticated but lacks permission. */
    FORBIDDEN(403, "Forbidden"),

    /** The requested resource was not found. */
    NOT_FOUND(404, "Not Found"),

    /** Internal server error — fall-through for unexpected failures. */
    INTERNAL_ERROR(500, "Internal Server Error");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /** Numeric HTTP-style status code. */
    public int getCode() {
        return code;
    }

    /** Human-readable default message. */
    public String getMessage() {
        return message;
    }
}