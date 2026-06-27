package com.xarch.example.common.feign;

import com.xarch.example.common.response.MicroResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback for {@code FileFeignClient} when {@code xarch-service-file} is
 * unavailable.
 *
 * <p>File upload fallbacks always return 503 — there is no safe local
 * substitute for an upload.
 */
@Slf4j
@Component
public class FileFeignFallback {

    /**
     * Fallback handler for file upload.
     *
     * @param fileName the original filename (used for diagnostics)
     * @return error envelope with code 503 and a descriptive message
     */
    public MicroResponse<Object> uploadFallback(String fileName) {
        log.warn("FileFeignClient.upload fallback triggered for fileName={}", fileName);
        return MicroResponse.fail(503, "xarch-service-file unavailable");
    }
}