package com.xarch.starter.storage.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Convenience base class for {@link StorageProvider} implementations.
 * <p>
 * Provides common helpers such as:
 * <ul>
 *   <li>Copying an {@link InputStream} to a byte array with MD5 computation.</li>
 *   <li>Computing an MD5 etag for a given byte array.</li>
 *   <li>Logging helpers for upload/download events.</li>
 * </ul>
 * </p>
 * Concrete providers extend this class to inherit shared behaviour and only
 * implement backend-specific logic.
 */
public abstract class AbstractStorageProvider implements StorageProvider {

    /** Default buffer size used for stream copies. */
    protected static final int DEFAULT_BUFFER_SIZE = 8192;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Read the entire content of the supplied stream into a byte array.
     *
     * @param input the input stream to drain
     * @return the content as a byte array
     * @throws StorageException if reading fails
     */
    protected byte[] readAllBytes(InputStream input) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        try {
            while ((read = input.read(data)) != -1) {
                buffer.write(data, 0, read);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to read input stream", e);
        }
        return buffer.toByteArray();
    }

    /**
     * Wrap a byte array in a {@link ByteArrayInputStream}.
     *
     * @param data the byte array
     * @return a new stream
     */
    protected InputStream toInputStream(byte[] data) {
        return new ByteArrayInputStream(data);
    }

    /**
     * Copy an input stream to an output stream using a buffered loop.
     * <p>
     * The streams are not closed by this method; callers should manage their
     * own lifecycle (typically with try-with-resources).
     * </p>
     *
     * @param input  source stream
     * @param output destination stream
     * @return the number of bytes copied
     * @throws IOException if the copy fails
     */
    protected long copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long total = 0;
        int read;
        try (BufferedOutputStream bos = new BufferedOutputStream(output)) {
            while ((read = input.read(buffer)) != -1) {
                bos.write(buffer, 0, read);
                total += read;
            }
        }
        return total;
    }

    /**
     * Compute the MD5 etag of the supplied bytes.
     *
     * @param data the bytes
     * @return the lowercase hex MD5 string, or null if MD5 is unavailable
     */
    protected String md5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data);
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.warn("MD5 algorithm not available, etag will be null");
            return null;
        }
    }

    /**
     * Ensure a string is not null and not blank, otherwise raise an
     * {@link StorageException}.
     *
     * @param value    the value to check
     * @param paramName the parameter name (for the error message)
     */
    protected void requireNonBlank(String value, String paramName) {
        if (value == null || value.isBlank()) {
            throw new StorageException("Parameter '" + paramName + "' must not be blank");
        }
    }
}
