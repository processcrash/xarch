package com.xarch.example.common.feign;

import com.xarch.example.common.response.MicroResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback for {@code UserFeignClient} when {@code xarch-service-auth} is
 * unavailable.
 *
 * <p>The fallback returns a {@link MicroResponse} with code 503 so callers
 * can distinguish "downstream missing" from a genuine empty result.
 */
@Slf4j
@Component
public class UserFeignFallback {

    /**
     * Fallback handler for user lookup.
     *
     * @param id the user id that was requested
     * @return error envelope with code 503 and a descriptive message
     */
    public MicroResponse<Object> getByIdFallback(Long id) {
        log.warn("UserFeignClient.getById fallback triggered for id={}", id);
        return MicroResponse.fail(503, "xarch-service-auth unavailable");
    }
}