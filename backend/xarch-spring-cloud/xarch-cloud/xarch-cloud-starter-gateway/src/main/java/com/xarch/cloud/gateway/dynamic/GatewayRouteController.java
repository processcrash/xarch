package com.xarch.cloud.gateway.dynamic;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Dynamic Gateway Route Controller
 * Provides REST API for managing routes at runtime
 */
@RestController
@RequestMapping("/gateway/routes")
public class GatewayRouteController {

    @Autowired
    private DynamicRouteService dynamicRouteService;

    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * Get all routes
     */
    @GetMapping
    public Mono<Result<List<RouteDefinition>>> getAllRoutes() {
        return Mono.fromSupplier(() -> Result.success(dynamicRouteService.getRoutes()));
    }

    /**
     * Get route by ID
     */
    @GetMapping("/{id}")
    public Mono<Result<RouteDefinition>> getRoute(@PathVariable String id) {
        RouteDefinition route = dynamicRouteService.getRoute(id);
        if (route == null) {
            return Mono.just(Result.fail("Route not found: " + id));
        }
        return Mono.just(Result.success(route));
    }

    /**
     * Add new route
     */
    @PostMapping
    public Mono<Result<String>> addRoute(@RequestBody RouteDefinition route) {
        try {
            dynamicRouteService.addRoute(route);
            // Publish refresh event to update routes
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.just(Result.success("Route added successfully"));
        } catch (Exception e) {
            return Mono.just(Result.fail("Failed to add route: " + e.getMessage()));
        }
    }

    /**
     * Update existing route
     */
    @PutMapping("/{id}")
    public Mono<Result<String>> updateRoute(@PathVariable String id, @RequestBody RouteDefinition route) {
        try {
            if (!id.equals(route.getId())) {
                return Mono.just(Result.fail("Route ID mismatch"));
            }
            dynamicRouteService.updateRoute(route);
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.just(Result.success("Route updated successfully"));
        } catch (Exception e) {
            return Mono.just(Result.fail("Failed to update route: " + e.getMessage()));
        }
    }

    /**
     * Delete route
     */
    @DeleteMapping("/{id}")
    public Mono<Result<String>> deleteRoute(@PathVariable String id) {
        try {
            if (!dynamicRouteService.hasRoute(id)) {
                return Mono.just(Result.fail("Route not found: " + id));
            }
            dynamicRouteService.deleteRoute(id);
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.just(Result.success("Route deleted successfully"));
        } catch (Exception e) {
            return Mono.just(Result.fail("Failed to delete route: " + e.getMessage()));
        }
    }

    /**
     * Reload routes from Nacos config
     */
    @PostMapping("/reload")
    public Mono<Result<String>> reloadRoutes() {
        try {
            publisher.publishEvent(new RefreshRoutesEvent(this));
            return Mono.just(Result.success("Routes reloaded successfully"));
        } catch (Exception e) {
            return Mono.just(Result.fail("Failed to reload routes: " + e.getMessage()));
        }
    }

    /**
     * Get routes count
     */
    @GetMapping("/count")
    public Mono<Result<Integer>> getRoutesCount() {
        return Mono.just(Result.success(dynamicRouteService.getRoutesCount()));
    }

    @Data
    public static class Result<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> Result<T> success(T data) {
            Result<T> result = new Result<>();
            result.setSuccess(true);
            result.setMessage("success");
            result.setData(data);
            return result;
        }

        public static <T> Result<T> success(String message) {
            Result<T> result = new Result<>();
            result.setSuccess(true);
            result.setMessage(message);
            return result;
        }

        public static <T> Result<T> fail(String message) {
            Result<T> result = new Result<>();
            result.setSuccess(false);
            result.setMessage(message);
            return result;
        }
    }
}
