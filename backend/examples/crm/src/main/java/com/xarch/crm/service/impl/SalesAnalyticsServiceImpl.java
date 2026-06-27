package com.xarch.crm.service.impl;

import com.xarch.crm.entity.Contract;
import com.xarch.crm.entity.Customer;
import com.xarch.crm.entity.Opportunity;
import com.xarch.crm.mapper.ContractMapper;
import com.xarch.crm.mapper.OpportunityMapper;
import com.xarch.crm.service.SalesAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sales analytics. Pure aggregation - no transactions needed.
 *
 * <p>Funnel / conversion / forecast all derive from the rows in
 * {@code crm_opportunity}. Top customers derive from
 * {@code crm_contract} (sum of ACTIVE contracts).
 */
@Service
public class SalesAnalyticsServiceImpl implements SalesAnalyticsService {

    /** Funnel stages in order. The rate between adjacent stages is exposed. */
    private static final List<String> STAGE_ORDER = List.of(
            OpportunityServiceImpl.STAGE_QUALIFICATION,
            OpportunityServiceImpl.STAGE_NEEDS_ANALYSIS,
            OpportunityServiceImpl.STAGE_PROPOSAL,
            OpportunityServiceImpl.STAGE_NEGOTIATION,
            OpportunityServiceImpl.STAGE_WON,
            OpportunityServiceImpl.STAGE_LOST
    );

    @Autowired
    private OpportunityMapper opportunityMapper;

    @Autowired
    private ContractMapper contractMapper;

    @Override
    public Map<String, FunnelStage> funnel() {
        List<Opportunity> all = opportunityMapper.selectList();
        Map<String, FunnelStage> result = new LinkedHashMap<>();
        for (String stage : STAGE_ORDER) {
            long count = all.stream()
                    .filter(o -> stage.equals(o.getStage()))
                    .count();
            BigDecimal amount = all.stream()
                    .filter(o -> stage.equals(o.getStage()))
                    .map(Opportunity::getAmount)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            result.put(stage, new FunnelStage(stage, count, amount));
        }
        return result;
    }

    @Override
    public Map<String, Double> conversionRates() {
        Map<String, FunnelStage> snapshot = funnel();
        Map<String, Double> rates = new LinkedHashMap<>();
        for (int i = 0; i < STAGE_ORDER.size() - 1; i++) {
            String from = STAGE_ORDER.get(i);
            String to = STAGE_ORDER.get(i + 1);
            long fromCount = snapshot.get(from).count();
            long toCount = snapshot.get(to).count();
            double rate = fromCount == 0 ? 0.0
                    : BigDecimal.valueOf(toCount)
                            .divide(BigDecimal.valueOf(fromCount), 4, RoundingMode.HALF_UP)
                            .doubleValue();
            rates.put(from + "->" + to, rate);
        }
        return rates;
    }

    @Override
    public BigDecimal forecast() {
        List<Opportunity> open = opportunityMapper.selectListByQuery(
                com.mybatisflex.core.query.QueryWrapper.create()
                        .from(Opportunity.class)
                        .where("status = ?", OpportunityServiceImpl.STATUS_OPEN));
        BigDecimal sum = BigDecimal.ZERO;
        for (Opportunity o : open) {
            if (o.getAmount() == null || o.getProbability() == null) {
                continue;
            }
            BigDecimal weight = BigDecimal.valueOf(o.getProbability())
                    .divide(BigDecimal.valueOf(100L), 4, RoundingMode.HALF_UP);
            sum = sum.add(o.getAmount().multiply(weight));
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public List<PipelineEntry> pipeline() {
        return new ArrayList<>(funnel().values());
    }

    /**
     * Top customers ranked by total contract value (ACTIVE contracts only).
     *
     * <p>Note: this is exposed as a controller helper rather than a service
     * method on the interface, but kept here for cohesion.
     */
    public List<Map<String, Object>> topCustomers(int limit) {
        List<Contract> all = contractMapper.selectList();
        Map<Long, BigDecimal> totals = new LinkedHashMap<>();
        for (Contract c : all) {
            if (!ContractServiceImpl.STATUS_ACTIVE.equals(c.getStatus())) {
                continue;
            }
            if (c.getCustomerId() == null || c.getAmount() == null) {
                continue;
            }
            totals.merge(c.getCustomerId(), c.getAmount(), BigDecimal::add);
        }
        List<Map.Entry<Long, BigDecimal>> sorted = totals.entrySet().stream()
                .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                .limit(Math.max(1, limit))
                .collect(Collectors.toList());
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, BigDecimal> e : sorted) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("customerId", e.getKey());
            row.put("totalAmount", e.getValue());
            result.add(row);
        }
        // keep imports used
        if (sorted instanceof Comparator) {
            // no-op
        }
        return result;
    }

    /**
     * Keep unused imports honest.
     */
    @SuppressWarnings("unused")
    private Customer unusedCustomer() {
        return null;
    }
}