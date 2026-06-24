package com.xarch.crm.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Sales analytics. Surfaces funnel, conversion rate and revenue
 * forecast data in a small, self-contained shape.
 */
public interface SalesAnalyticsService {

    /**
     * Build a funnel snapshot: stage -> (count, amount).
     */
    Map<String, FunnelStage> funnel();

    /**
     * Stage-to-stage conversion rates. The key is "FROM->TO".
     */
    Map<String, Double> conversionRates();

    /**
     * Weighted revenue forecast, summing {@code amount * probability}
     * over all open opportunities.
     */
    BigDecimal forecast();

    /**
     * Pipeline summary - the count / amount of every stage.
     */
    List<PipelineEntry> pipeline();

    /** Funnel bucket for a single stage. */
    record FunnelStage(String stage, long count, BigDecimal amount) {
    }

    /** Flat pipeline row. */
    record PipelineEntry(String stage, long count, BigDecimal amount) {
    }
}
