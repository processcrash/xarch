package com.xarch.example.auth.client;

import com.xarch.example.auth.entity.User;
import com.xarch.starter.core.result.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client for the user endpoints exposed by {@code service-auth}.
 *
 * <p>Other micro-services depend on this client to resolve user names
 * from ids without joining across databases.
 */
@FeignClient(name = "xarch-service-auth", path = "/api/users")
public interface UserFeignClient {

    /**
     * Fetch a user by id.
     *
     * @param id user id
     * @return wrapped user payload
     */
    @GetMapping("/{id}")
    ApiResult<User> getById(@PathVariable("id") Long id);
}