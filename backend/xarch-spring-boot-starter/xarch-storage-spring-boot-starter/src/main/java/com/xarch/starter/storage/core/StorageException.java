package com.xarch.starter.storage.core;

/**
 * Runtime exception thrown by storage provider implementations.
 * <p>
 * All checked and unchecked exceptions raised by underlying SDKs are
 * translated into this unchecked exception so callers can rely on a single
 * exception type when handling storage failures.
 * </p>
 */
public class StorageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Create a new storage exception with the given message.
     *
     * @param message the error message
     */
    public StorageException(String message) {
        super(message);
    }

    /**
     * Create a new storage exception with the given message and cause.
     *
     * @param message the error message
     * @param cause   the underlying cause
     */
    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
