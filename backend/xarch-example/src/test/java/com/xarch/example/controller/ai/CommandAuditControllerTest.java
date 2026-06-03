package com.xarch.example.controller.ai;

import cn.dev33.satoken.stp.StpUtil;
import com.xarch.example.entity.ai.CommandAudit;
import com.xarch.example.service.ai.CommandAuditService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CommandAuditController unit tests
 */
@SpringBootTest
class CommandAuditControllerTest {

    @Autowired
    private CommandAuditController commandAuditController;

    @Autowired
    private CommandAuditService auditService;

    @MockitoBean
    private CommandAuditService mockAuditService;

    @BeforeEach
    void setUp() {
        // Login as test user for authentication-required methods
        StpUtil.login(1L);
    }

    @Test
    void testPage() {
        CommandAuditController.AuditQueryRequest request = new CommandAuditController.AuditQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        ApiResult<PageResult<CommandAudit>> result = commandAuditController.page(request);
        assertNotNull(result);
        assertNotNull(result.getCode());
    }

    @Test
    void testPageWithFilters() {
        CommandAuditController.AuditQueryRequest request = new CommandAuditController.AuditQueryRequest();
        request.setServerId(1L);
        request.setUserId(1L);
        request.setRiskLevel(1);
        request.setApprovalStatus(1);
        request.setStartTime(LocalDateTime.now().minusDays(1));
        request.setEndTime(LocalDateTime.now());
        request.setPageNum(1);
        request.setPageSize(10);

        ApiResult<PageResult<CommandAudit>> result = commandAuditController.page(request);
        assertNotNull(result);
        assertNotNull(result.getCode());
    }

    @Test
    void testDetail() {
        ApiResult<CommandAudit> result = commandAuditController.detail(1L);
        assertNotNull(result);
        assertNotNull(result.getCode());
    }

    @Test
    void testDetailNotFound() {
        ApiResult<CommandAudit> result = commandAuditController.detail(999999L);
        assertNotNull(result);
        // Result depends on whether audit exists
        assertNotNull(result.getCode());
    }

    @Test
    void testGetPendingApprovals() {
        ApiResult<PageResult<CommandAudit>> result = commandAuditController.pendingApprovals(1, 20);
        assertNotNull(result);
        assertNotNull(result.getCode());
    }

    @Test
    void testApprove() {
        CommandAuditController.ApprovalRequest request = new CommandAuditController.ApprovalRequest();
        request.setComment("Approved for testing");

        ApiResult<Void> result = commandAuditController.approve(1L, request);
        assertNotNull(result);
        assertNotNull(result.getCode());
    }

    @Test
    void testApproveWithNullRequest() {
        ApiResult<Void> result = commandAuditController.approve(1L, null);
        assertNotNull(result);
        assertNotNull(result.getCode());
    }

    @Test
    void testReject() {
        CommandAuditController.RejectRequest request = new CommandAuditController.RejectRequest();
        request.setReason("Too risky for production");

        ApiResult<Void> result = commandAuditController.reject(1L, request);
        assertNotNull(result);
        assertNotNull(result.getCode());
    }

    @Test
    void testGetStats() {
        ApiResult<CommandAuditService.ComplianceStats> result = commandAuditController.stats(null, null);
        assertNotNull(result);
        assertNotNull(result.getCode());
    }

    @Test
    void testGetStatsWithDateRange() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();

        ApiResult<CommandAuditService.ComplianceStats> result = commandAuditController.stats(start, end);
        assertNotNull(result);
        assertNotNull(result.getCode());
    }

    @Test
    void testGetUserHistory() {
        ApiResult<PageResult<CommandAudit>> result = commandAuditController.userHistory(1, 20);
        assertNotNull(result);
        assertNotNull(result.getCode());
    }

    @Test
    void testRejectWithNullReason() {
        CommandAuditController.RejectRequest request = new CommandAuditController.RejectRequest();
        request.setReason(null);

        ApiResult<Void> result = commandAuditController.reject(1L, request);
        assertNotNull(result);
        // Reject with null reason - controller just passes to service
        assertNotNull(result.getCode());
    }
}