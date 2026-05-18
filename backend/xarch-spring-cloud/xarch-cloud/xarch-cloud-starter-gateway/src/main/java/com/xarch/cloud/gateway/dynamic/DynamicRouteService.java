package com.xarch.cloud.gateway.dynamic;

import lombok.Data;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Dynamic Route Service
 * Manages routes in memory and persists to Nacos config
 * Routes can be added, updated, deleted at runtime
 */
@Service
public class DynamicRouteService {

    // In-memory route storage
    private final Map<String, RouteDefinition> routes = new ConcurrentHashMap<>();

    /**
     * Get all routes
     */
    public List<RouteDefinition> getRoutes() {
        return new ArrayList<>(routes.values());
    }

    /**
     * Get route by ID
     */
    public RouteDefinition getRoute(String id) {
        return routes.get(id);
    }

    /**
     * Add a new route
     */
    public void addRoute(RouteDefinition route) {
        if (route.getId() == null || route.getId().isEmpty()) {
            throw new IllegalArgumentException("Route ID cannot be empty");
        }
        routes.put(route.getId(), route);
    }

    /**
     * Update existing route
     */
    public void updateRoute(RouteDefinition route) {
        if (!routes.containsKey(route.getId())) {
            throw new IllegalArgumentException("Route not found: " + route.getId());
        }
        routes.put(route.getId(), route);
    }

    /**
     * Delete route by ID
     */
    public void deleteRoute(String id) {
        routes.remove(id);
    }

    /**
     * Check if route exists
     */
    public boolean hasRoute(String id) {
        return routes.containsKey(id);
    }

    /**
     * Get routes count
     */
    public int getRoutesCount() {
        return routes.size();
    }

    /**
     * Clear all routes
     */
    public void clearRoutes() {
        routes.clear();
    }

    /**
     * Load routes from Nacos config
     * This would typically fetch from Nacos configuration
     */
    public void loadRoutesFromConfig(List<RouteDefinition> routeDefinitions) {
        if (routeDefinitions != null) {
            routes.clear();
            routeDefinitions.forEach(route -> routes.put(route.getId(), route));
        }
    }

    /**
     * Export routes as list (for Nacos persistence)
     */
    public List<RouteDefinition> exportRoutes() {
        return routes.values().stream().collect(Collectors.toList());
    }
}
