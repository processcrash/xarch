package com.xarch.example.ai.service;

import com.xarch.example.ai.entity.CommandAudit;
import com.xarch.starter.core.result.PageResult;

import java.time.LocalDateTime;

/** Command audit service contract. */
public interface CommandAuditService {
    PageResult<CommandAudit> getAuditLogs(AuditQueryParams params);
    CommandAudit getById(Long id);
    PageResult<CommandAudit> getPendingApprovals(int pageNum, int pageSize);
    boolean approve(Long id, String comment);
    boolean reject(Long id, String reason);
    ComplianceStats getComplianceStats(LocalDateTime start, LocalDateTime end);
    PageResult<CommandAudit> getUserHistory(Long userId, int pageNum, int pageSize);

    /** Audit query parameter bundle. */
    final class AuditQueryParams {
        private Long serverId;
        private Long userId;
        private Integer riskLevel;
        private Integer approvalStatus;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int pageNum = 1;
        private int pageSize = 20;

        public Long getServerId() { return serverId; }
        public void setServerId(Long serverId) { this.serverId = serverId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Integer getRiskLevel() { return riskLevel; }
        public void setRiskLevel(Integer riskLevel) { this.riskLevel = riskLevel; }
        public Integer getApprovalStatus() { return approvalStatus; }
        public void setApprovalStatus(Integer approvalStatus) { this.approvalStatus = approvalStatus; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public int getPageNum() { return pageNum; }
        public void setPageNum(int pageNum) { this.pageNum = pageNum; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    }

    /** Aggregate compliance metrics. */
    final class ComplianceStats {
        public long totalAudits;
        public long pendingCount;
        public long approvedCount;
        public long rejectedCount;
        public long highRiskCount;
    }
}