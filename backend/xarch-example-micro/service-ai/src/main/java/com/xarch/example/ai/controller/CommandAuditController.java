package com.xarch.example.ai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xarch.example.ai.entity.CommandAudit;
import com.xarch.example.ai.service.CommandAuditService;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Command Audit Controller — handles audit logging, approval workflow,
 * and compliance reporting.
 *
 * <p>Migrated verbatim from monolith. Package relocated to
 * {@code com.xarch.example.ai.controller}.
 */
@Tag(name = "AI Command Audit", description = "Command audit and approval workflow")
@RestController
@RequestMapping("/ai/audit")
@RequiredArgsConstructor
public class CommandAuditController {

    private final CommandAuditService auditService;

    @GetMapping("/page")
    @Operation(summary = "Query audit logs")
    public ApiResult<PageResult<CommandAudit>> page(AuditQueryRequest request) {
        CommandAuditService.AuditQueryParams params = new CommandAuditService.AuditQueryParams();
        params.setServerId(request.getServerId());
        params.setUserId(request.getUserId());
        params.setRiskLevel(request.getRiskLevel());
        params.setApprovalStatus(request.getApprovalStatus());
        params.setStartTime(request.getStartTime());
        params.setEndTime(request.getEndTime());
        params.setPageNum(request.getPageNum());
        params.setPageSize(request.getPageSize());

        return ApiResult.success(auditService.getAuditLogs(params));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get audit detail")
    public ApiResult<CommandAudit> detail(@PathVariable Long id) {
        CommandAudit audit = auditService.getById(id);
        if (audit == null) {
            return ApiResult.fail("Audit record not found");
        }
        return ApiResult.success(audit);
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending approvals")
    public ApiResult<PageResult<CommandAudit>> pendingApprovals(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.success(auditService.getPendingApprovals(pageNum, pageSize));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve command")
    public ApiResult<Void> approve(@PathVariable Long id, @RequestBody(required = false) ApprovalRequest request) {
        String comment = request != null ? request.getComment() : null;
        boolean success = auditService.approve(id, comment);
        return success ? ApiResult.success(null) : ApiResult.fail("Failed to approve");
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject command")
    public ApiResult<Void> reject(@PathVariable Long id, @RequestBody RejectRequest request) {
        boolean success = auditService.reject(id, request.getReason());
        return success ? ApiResult.success(null) : ApiResult.fail("Failed to reject");
    }

    @GetMapping("/stats")
    @Operation(summary = "Get compliance statistics")
    public ApiResult<CommandAuditService.ComplianceStats> stats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ApiResult.success(auditService.getComplianceStats(start, end));
    }

    @GetMapping("/user/history")
    @Operation(summary = "Get current user's command history")
    public ApiResult<PageResult<CommandAudit>> userHistory(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResult.success(auditService.getUserHistory(userId, pageNum, pageSize));
    }

    @Data
    public static class AuditQueryRequest {
        private Long serverId;
        private Long userId;
        private Integer riskLevel;
        private Integer approvalStatus;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int pageNum = 1;
        private int pageSize = 20;
    }

    @Data
    public static class ApprovalRequest {
        private String comment;
    }

    @Data
    public static class RejectRequest {
        private String reason;
    }
}