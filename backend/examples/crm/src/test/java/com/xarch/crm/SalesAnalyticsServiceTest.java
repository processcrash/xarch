package com.xarch.crm;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.crm.entity.Contract;
import com.xarch.crm.entity.Opportunity;
import com.xarch.crm.mapper.ContractMapper;
import com.xarch.crm.mapper.OpportunityMapper;
import com.xarch.crm.service.SalesAnalyticsService;
import com.xarch.crm.service.impl.ContractServiceImpl;
import com.xarch.crm.service.impl.OpportunityServiceImpl;
import com.xarch.crm.service.impl.SalesAnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link SalesAnalyticsServiceImpl}. The mappers
 * are replaced with Mockito stubs so the service can be exercised
 * without spinning up a Spring context.
 */
class SalesAnalyticsServiceTest {

    private OpportunityMapper opportunityMapper;
    private ContractMapper contractMapper;
    private SalesAnalyticsServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        opportunityMapper = mock(OpportunityMapper.class);
        contractMapper = mock(ContractMapper.class);
        service = new SalesAnalyticsServiceImpl();
        setField(service, "opportunityMapper", opportunityMapper);
        setField(service, "contractMapper", contractMapper);
    }

    @Test
    void funnelAggregatesByStage() {
        when(opportunityMapper.selectList()).thenReturn(List.of(
                opp("A", OpportunityServiceImpl.STAGE_QUALIFICATION, "100"),
                opp("B", OpportunityServiceImpl.STAGE_QUALIFICATION, "200"),
                opp("C", OpportunityServiceImpl.STAGE_PROPOSAL, "500"),
                opp("D", OpportunityServiceImpl.STAGE_WON, "1000")
        ));
        Map<String, SalesAnalyticsService.FunnelStage> funnel = service.funnel();
        assertNotNull(funnel);
        assertEquals(2L, funnel.get(OpportunityServiceImpl.STAGE_QUALIFICATION).count());
        assertEquals(new BigDecimal("300"),
                funnel.get(OpportunityServiceImpl.STAGE_QUALIFICATION).amount());
        assertEquals(1L, funnel.get(OpportunityServiceImpl.STAGE_WON).count());
        assertEquals(new BigDecimal("1000"),
                funnel.get(OpportunityServiceImpl.STAGE_WON).amount());
    }

    @Test
    void conversionRatesUsesAdjacentCounts() {
        when(opportunityMapper.selectList()).thenReturn(List.of(
                opp("A", OpportunityServiceImpl.STAGE_QUALIFICATION, "100"),
                opp("B", OpportunityServiceImpl.STAGE_QUALIFICATION, "100"),
                opp("C", OpportunityServiceImpl.STAGE_NEEDS_ANALYSIS, "100"),
                opp("D", OpportunityServiceImpl.STAGE_PROPOSAL, "100")
        ));
        Map<String, Double> rates = service.conversionRates();
        // QUALIFICATION -> NEEDS_ANALYSIS: 1/2 = 0.5
        assertEquals(0.5,
                rates.get(OpportunityServiceImpl.STAGE_QUALIFICATION + "->"
                        + OpportunityServiceImpl.STAGE_NEEDS_ANALYSIS),
                0.0001);
    }

    @Test
    void forecastSumsAmountTimesProbability() {
        when(opportunityMapper.selectListByQuery(any(QueryWrapper.class))).thenReturn(List.of(
                opp("A", OpportunityServiceImpl.STAGE_PROPOSAL, "1000"),
                opp("B", OpportunityServiceImpl.STAGE_NEGOTIATION, "2000")
        ));
        BigDecimal forecast = service.forecast();
        assertNotNull(forecast);
    }

    @Test
    void topCustomersAggregatesByContractValue() {
        when(contractMapper.selectList()).thenReturn(List.of(
                contract(1L, "1000", ContractServiceImpl.STATUS_ACTIVE),
                contract(1L, "500", ContractServiceImpl.STATUS_ACTIVE),
                contract(2L, "200", ContractServiceImpl.STATUS_ACTIVE),
                contract(3L, "9999", ContractServiceImpl.STATUS_TERMINATED) // excluded
        ));
        List<Map<String, Object>> top = service.topCustomers(5);
        assertEquals(2, top.size());
        assertEquals(1L, top.get(0).get("customerId"));
        assertEquals(new BigDecimal("1500"), top.get(0).get("totalAmount"));
        assertEquals(2L, top.get(1).get("customerId"));
    }

    @Test
    void pipelineMirrorsFunnel() {
        when(opportunityMapper.selectList()).thenReturn(List.of(
                opp("A", OpportunityServiceImpl.STAGE_QUALIFICATION, "100"),
                opp("B", OpportunityServiceImpl.STAGE_WON, "200")
        ));
        List<SalesAnalyticsService.PipelineEntry> pipeline = service.pipeline();
        assertEquals(6, pipeline.size()); // 6 stages
        assertEquals(OpportunityServiceImpl.STAGE_QUALIFICATION, pipeline.get(0).stage());
    }

    @Nested
    @DisplayName("Extended analytics tests")
    class Extended {

        @Test
        @DisplayName("getFunnel returns counts by stage")
        void shouldReturnFunnelCounts() {
            when(opportunityMapper.selectList()).thenReturn(List.of(
                    opp("A", OpportunityServiceImpl.STAGE_QUALIFICATION, "100"),
                    opp("B", OpportunityServiceImpl.STAGE_QUALIFICATION, "200"),
                    opp("C", OpportunityServiceImpl.STAGE_WON, "5000"),
                    opp("D", OpportunityServiceImpl.STAGE_WON, "3000"),
                    opp("E", OpportunityServiceImpl.STAGE_LOST, "500")
            ));

            Map<String, SalesAnalyticsService.FunnelStage> funnel = service.funnel();

            assertThat(funnel.get(OpportunityServiceImpl.STAGE_QUALIFICATION).count()).isEqualTo(2L);
            assertThat(funnel.get(OpportunityServiceImpl.STAGE_WON).count()).isEqualTo(2L);
            assertThat(funnel.get(OpportunityServiceImpl.STAGE_LOST).count()).isEqualTo(1L);
            assertThat(funnel.get(OpportunityServiceImpl.STAGE_NEEDS_ANALYSIS).count()).isEqualTo(0L);
            assertThat(funnel.get(OpportunityServiceImpl.STAGE_WON).amount())
                    .isEqualByComparingTo("8000");
        }

        @Test
        @DisplayName("getFunnel returns all stages even when missing")
        void shouldReturnAllStages() {
            when(opportunityMapper.selectList()).thenReturn(List.of());

            Map<String, SalesAnalyticsService.FunnelStage> funnel = service.funnel();

            assertThat(funnel).hasSize(6);
        }

        @Test
        @DisplayName("getConversionRate computes stage-to-stage percentages")
        void shouldComputeConversionRates() {
            when(opportunityMapper.selectList()).thenReturn(List.of(
                    opp("A", OpportunityServiceImpl.STAGE_QUALIFICATION, "100"),
                    opp("B", OpportunityServiceImpl.STAGE_QUALIFICATION, "100"),
                    opp("C", OpportunityServiceImpl.STAGE_NEEDS_ANALYSIS, "100"),
                    opp("D", OpportunityServiceImpl.STAGE_PROPOSAL, "100"),
                    opp("E", OpportunityServiceImpl.STAGE_NEGOTIATION, "100")
            ));

            Map<String, Double> rates = service.conversionRates();

            // QUALIFICATION -> NEEDS_ANALYSIS: 1/2 = 0.5
            assertThat(rates.get(OpportunityServiceImpl.STAGE_QUALIFICATION + "->"
                    + OpportunityServiceImpl.STAGE_NEEDS_ANALYSIS)).isEqualTo(0.5);
            // NEEDS_ANALYSIS -> PROPOSAL: 1/1 = 1.0
            assertThat(rates.get(OpportunityServiceImpl.STAGE_NEEDS_ANALYSIS + "->"
                    + OpportunityServiceImpl.STAGE_PROPOSAL)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("getConversionRate returns 0 when from-count is 0")
        void shouldReturnZero_whenFromEmpty() {
            when(opportunityMapper.selectList()).thenReturn(List.of());

            Map<String, Double> rates = service.conversionRates();

            // First rate should be 0.0 because QUALIFICATION count is 0.
            String firstKey = OpportunityServiceImpl.STAGE_QUALIFICATION + "->"
                    + OpportunityServiceImpl.STAGE_NEEDS_ANALYSIS;
            assertThat(rates.get(firstKey)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("getRevenueForecast sums amount * probability")
        void shouldForecastRevenue() {
            Opportunity a = openOpp("A", "1000", 50);
            Opportunity b = openOpp("B", "2000", 25);
            when(opportunityMapper.selectListByQuery(any(QueryWrapper.class)))
                    .thenReturn(List.of(a, b));

            BigDecimal forecast = service.forecast();

            // 1000 * 0.5 + 2000 * 0.25 = 500 + 500 = 1000
            assertThat(forecast).isEqualByComparingTo("1000.00");
        }

        @Test
        @DisplayName("forecast returns zero when no open opportunities")
        void shouldReturnZero_whenNoOpen() {
            when(opportunityMapper.selectListByQuery(any(QueryWrapper.class)))
                    .thenReturn(List.of());

            assertThat(service.forecast()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("forecast skips rows with null amount or probability")
        void shouldSkipInvalidRows() {
            Opportunity valid = openOpp("A", "1000", 50);
            Opportunity noAmount = openOpp("B", null, 50);
            Opportunity noProb = openOpp("C", "2000", null);
            when(opportunityMapper.selectListByQuery(any(QueryWrapper.class)))
                    .thenReturn(List.of(valid, noAmount, noProb));

            BigDecimal forecast = service.forecast();

            assertThat(forecast).isEqualByComparingTo("500.00");
        }

        @Test
        @DisplayName("getTopCustomers returns top N by contract value")
        void shouldReturnTopN() {
            when(contractMapper.selectList()).thenReturn(List.of(
                    contract(1L, "1000", ContractServiceImpl.STATUS_ACTIVE),
                    contract(1L, "500", ContractServiceImpl.STATUS_ACTIVE),
                    contract(2L, "200", ContractServiceImpl.STATUS_ACTIVE),
                    contract(3L, "9999", ContractServiceImpl.STATUS_TERMINATED),
                    contract(4L, "7777", ContractServiceImpl.STATUS_ACTIVE),
                    contract(5L, "100", ContractServiceImpl.STATUS_ACTIVE)
            ));

            List<Map<String, Object>> top = service.topCustomers(3);

            assertThat(top).hasSize(3);
            assertThat(top.get(0).get("customerId")).isEqualTo(4L);
            assertThat(top.get(0).get("totalAmount")).isEqualByComparingTo("7777");
            assertThat(top.get(1).get("customerId")).isEqualTo(1L);
            assertThat(top.get(1).get("totalAmount")).isEqualByComparingTo("1500");
            assertThat(top.get(2).get("customerId")).isEqualTo(5L);
        }

        @Test
        @DisplayName("topCustomers excludes non-ACTIVE contracts")
        void shouldExcludeNonActive() {
            when(contractMapper.selectList()).thenReturn(List.of(
                    contract(1L, "100", ContractServiceImpl.STATUS_DRAFT),
                    contract(1L, "50", ContractServiceImpl.STATUS_EXPIRED),
                    contract(2L, "200", ContractServiceImpl.STATUS_TERMINATED)
            ));

            List<Map<String, Object>> top = service.topCustomers(5);

            assertThat(top).isEmpty();
        }

        @Test
        @DisplayName("topCustomers aggregates by customer")
        void shouldAggregateByCustomer() {
            when(contractMapper.selectList()).thenReturn(List.of(
                    contract(1L, "100", ContractServiceImpl.STATUS_ACTIVE),
                    contract(1L, "200", ContractServiceImpl.STATUS_ACTIVE),
                    contract(1L, "300", ContractServiceImpl.STATUS_ACTIVE),
                    contract(2L, "50", ContractServiceImpl.STATUS_ACTIVE)
            ));

            List<Map<String, Object>> top = service.topCustomers(5);

            assertThat(top).hasSize(2);
            assertThat(top.get(0).get("customerId")).isEqualTo(1L);
            assertThat(top.get(0).get("totalAmount")).isEqualByComparingTo("600");
            assertThat(top.get(1).get("customerId")).isEqualTo(2L);
            assertThat(top.get(1).get("totalAmount")).isEqualByComparingTo("50");
        }
    }

    /**
     * Build a sample Opportunity with a probability.
     */
    private Opportunity opp(String name, String stage, String amount) {
        Opportunity o = new Opportunity();
        o.setName(name);
        o.setStage(stage);
        o.setAmount(new BigDecimal(amount));
        o.setProbability(stage.equals(OpportunityServiceImpl.STAGE_WON) ? 100 : 10);
        return o;
    }

    /**
     * Build an OPEN opportunity with explicit amount/probability (for forecast).
     */
    private Opportunity openOpp(String name, String amount, Integer probability) {
        Opportunity o = new Opportunity();
        o.setName(name);
        o.setStage(OpportunityServiceImpl.STAGE_QUALIFICATION);
        o.setStatus(OpportunityServiceImpl.STATUS_OPEN);
        if (amount != null) {
            o.setAmount(new BigDecimal(amount));
        }
        o.setProbability(probability);
        return o;
    }

    /**
     * Build a sample Contract.
     */
    private Contract contract(Long customerId, String amount, String status) {
        Contract c = new Contract();
        c.setCustomerId(customerId);
        c.setAmount(new BigDecimal(amount));
        c.setStatus(status);
        return c;
    }

    /**
     * Inject a private field via reflection - cleaner than a setter.
     */
    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}