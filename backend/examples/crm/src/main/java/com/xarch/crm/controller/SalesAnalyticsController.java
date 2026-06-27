package com.xarch.crm.controller;

import com.xarch.crm.service.SalesAnalyticsService;
import com.xarch.crm.service.impl.SalesAnalyticsServiceImpl;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Sales analytics REST endpoints. Funnel / conversion / forecast /
 * top customers, all aggregated on the fly from the opportunity
 * and contract tables.
 */
@RestController
@RequestMapping("/api/analytics")
@Tag(name = "SalesAnalytics")
public class SalesAnalyticsController {

    @Autowired
    private SalesAnalyticsService salesAnalyticsService;

    @Autowired
    private SalesAnalyticsServiceImpl salesAnalyticsServiceImpl;

    @GetMapping("/funnel")
    @XarchLog(value = "Get funnel snapshot", type = "QUERY")
    public ApiResult<Map<String, SalesAnalyticsService.FunnelStage>> funnel() {
        return ApiResult.ok(salesAnalyticsService.funnel());
    }

    @GetMapping("/conversion")
    @XarchLog(value = "Get conversion rates", type = "QUERY")
    public ApiResult<Map<String, Double>> conversion() {
        return ApiResult.ok(salesAnalyticsService.conversionRates());
    }

    @GetMapping("/forecast")
    @XarchLog(value = "Get revenue forecast", type = "QUERY")
    public ApiResult<BigDecimal> forecast() {
        return ApiResult.ok(salesAnalyticsService.forecast());
    }

    @GetMapping("/pipeline")
    @XarchLog(value = "Get pipeline summary", type = "QUERY")
    public ApiResult<List<SalesAnalyticsService.PipelineEntry>> pipeline() {
        return ApiResult.ok(salesAnalyticsService.pipeline());
    }

    /**
     * Top customers by total contract value (ACTIVE contracts).
     */
    @GetMapping("/top-customers")
    @XarchLog(value = "Get top customers", type = "QUERY")
    public ApiResult<List<Map<String, Object>>> topCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResult.ok(salesAnalyticsServiceImpl.topCustomers(limit));
    }
}