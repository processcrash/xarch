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
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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