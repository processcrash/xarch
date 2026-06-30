package com.xarch.starter.storage.local;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the local filesystem provider.
 */
@Data
@ConfigurationProperties(prefix = "xarch.storage.configs.local")
public class LocalStorageProperties {

    /**
     * Base directory on the local filesystem where objects are stored.
     * <p>
     * The path may be absolute or relative to the JVM working directory.
     * The directory will be created on startup if it does not exist.
     * </p>
     */
    private String basePath = "./xarch-files";

    /**
     * Public URL prefix used to build access URLs.
     * <p>
     * The resulting URL for an object is
     * {@code <publicBaseUrl>/<objectKey>}. Set this to the URL prefix under
     * which the local files are served (e.g. a Spring static-resource
     * mapping or a reverse-proxy).
     * </p>
     */
    private String publicBaseUrl = "/files";
}
