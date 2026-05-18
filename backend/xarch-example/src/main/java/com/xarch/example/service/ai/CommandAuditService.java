package com.xarch.example.service.ai;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.ai.CommandAudit;
import com.xarch.example.entity.ai.Server;
import com.xarch.example.mapper.ai.CommandAuditMapper;
import com.xarch.starter.core.result.PageResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Command Audit Service
 * Handles command audit logging, approval workflow, and compliance reporting
 */
@Service
@Slf4j
public class CommandAuditService {

    @Autowired
    private CommandAuditMapper auditMapper;

    @Autowired
    private AiAgentService aiAgentService;

    /**
     * Record command execution with risk assessment
     */
    public CommandAudit recordExecution(CommandExecutionContext context) {
        CommandAudit audit = new CommandAudit();
        audit.setServerId(context.getServerId());
        audit.setServerName(context.getServerName());
        audit.setUserId(StpUtil.getLoginIdAsLong());
        audit.setUserName(StpUtil.getLoginIdAsString());
        audit.setCommand(context.getCommand());
        audit.setAiGeneratedCommand(context.getAiGeneratedCommand());
        audit.setAiPrompt(context.getAiPrompt());
        audit.setOutput(context.getOutput());
        audit.setExitCode(context.getExitCode());
        audit.setDuration(context.getDuration());
        audit.setSessionId(context.getSessionId());
        audit.setUserIp(context.getUserIp());
        audit.setUserAgent(context.getUserAgent());
        audit.setStatus(context.getStatus());
        audit.setDelFlag(0);
        audit.setCreateTime(LocalDateTime.now());

        // Risk assessment
        AiAgentService.SafetyValidation validation = aiAgentService.validateCommand(context.getCommand());
        audit.setRiskLevel(mapSafetyToRiskLevel(validation.getLevel()));
        audit.setApprovalStatus(0); // Pending by default for high-risk commands

        // Auto-approve low-risk commands
        if (audit.getRiskLevel() <= 1) {
            audit.setApprovalStatus(1); // Approved
        }

        auditMapper.insert(audit);
        log.info("Recorded command audit: server={}, command={}, risk={}",
                context.getServerName(), context.getCommand(), audit.getRiskLevel());

        return audit;
    }

    /**
     * Request approval for high-risk command
     */
    public void requestApproval(Long auditId, String reason) {
        CommandAudit audit = auditMapper.selectById(auditId);
        if (audit != null) {
            audit.setApprovalStatus(0); // Pending
            audit.setApprovalComment(reason);
            auditMapper.updateById(audit);
            log.info("Approval requested for audit: {}", auditId);
        }
    }

    /**
     * Approve command
     */
    public boolean approve(Long auditId, String comment) {
        CommandAudit audit = auditMapper.selectById(auditId);
        if (audit == null) return false;

        audit.setApprovalStatus(1); // Approved
        audit.setApprovedBy(StpUtil.getLoginIdAsLong());
        audit.setApprovedByName(StpUtil.getLoginIdAsString());
        audit.setApprovedTime(LocalDateTime.now());
        audit.setApprovalComment(comment);
        auditMapper.updateById(audit);

        log.info("Command approved: auditId={}, approvedBy={}", auditId, StpUtil.getLoginIdAsString());
        return true;
    }

    /**
     * Reject command
     */
    public boolean reject(Long auditId, String reason) {
        CommandAudit audit = auditMapper.selectById(auditId);
        if (audit == null) return false;

        audit.setApprovalStatus(2); // Rejected
        audit.setApprovedBy(StpUtil.getLoginIdAsLong());
        audit.setApprovedByName(StpUtil.getLoginIdAsString());
        audit.setApprovedTime(LocalDateTime.now());
        audit.setApprovalComment(reason);
        auditMapper.updateById(audit);

        log.info("Command rejected: auditId={}, reason={}", auditId, reason);
        return true;
    }

    /**
     * Get pending approvals
     */
    public PageResult<CommandAudit> getPendingApprovals(int pageNum, int pageSize) {
        LambdaQueryWrapper<CommandAudit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommandAudit::getApprovalStatus, 0)
               .eq(CommandAudit::getDelFlag, 0)
               .orderByDesc(CommandAudit::getCreateTime);

        Page<CommandAudit> page = new Page<>(pageNum, pageSize);
        Page<CommandAudit> result = auditMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    /**
     * Get audit logs with filters
     */
    public PageResult<CommandAudit> getAuditLogs(AuditQueryParams params) {
        LambdaQueryWrapper<CommandAudit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommandAudit::getDelFlag, 0);

        if (params.getServerId() != null) {
            wrapper.eq(CommandAudit::getServerId, params.getServerId());
        }
        if (params.getUserId() != null) {
            wrapper.eq(CommandAudit::getUserId, params.getUserId());
        }
        if (params.getRiskLevel() != null) {
            wrapper.eq(CommandAudit::getRiskLevel, params.getRiskLevel());
        }
        if (params.getApprovalStatus() != null) {
            wrapper.eq(CommandAudit::getApprovalStatus, params.getApprovalStatus());
        }
        if (params.getStartTime() != null) {
            wrapper.ge(CommandAudit::getCreateTime, params.getStartTime());
        }
        if (params.getEndTime() != null) {
            wrapper.le(CommandAudit::getCreateTime, params.getEndTime());
        }

        wrapper.orderByDesc(CommandAudit::getCreateTime);

        Page<CommandAudit> page = new Page<>(params.getPageNum(), params.getPageSize());
        Page<CommandAudit> result = auditMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    /**
     * Get audit by ID
     */
    public CommandAudit getById(Long id) {
        return auditMapper.selectById(id);
    }

    /**
     * Get user's command history
     */
    public PageResult<CommandAudit> getUserHistory(Long userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<CommandAudit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommandAudit::getUserId, userId)
               .eq(CommandAudit::getDelFlag, 0)
               .orderByDesc(CommandAudit::getCreateTime);

        Page<CommandAudit> page = new Page<>(pageNum, pageSize);
        Page<CommandAudit> result = auditMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    /**
     * Get compliance statistics
     */
    public ComplianceStats getComplianceStats(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<CommandAudit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommandAudit::getDelFlag, 0);
        if (start != null) wrapper.ge(CommandAudit::getCreateTime, start);
        if (end != null) wrapper.le(CommandAudit::getCreateTime, end);

        List<CommandAudit> audits = auditMapper.selectList(wrapper);

        ComplianceStats stats = new ComplianceStats();
        stats.setTotalCommands(audits.size());

        long approved = audits.stream().filter(a -> a.getApprovalStatus() == 1).count();
        long rejected = audits.stream().filter(a -> a.getApprovalStatus() == 2).count();
        long pending = audits.stream().filter(a -> a.getApprovalStatus() == 0).count();
        long highRisk = audits.stream().filter(a -> a.getRiskLevel() >= 3).count();

        stats.setApprovedCount((int) approved);
        stats.setRejectedCount((int) rejected);
        stats.setPendingCount((int) pending);
        stats.setHighRiskCount((int) highRisk);

        return stats;
    }

    private int mapSafetyToRiskLevel(AiAgentService.SafetyLevel level) {
        return switch (level) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case WARNING -> 3;
            case HIGH -> 4;
        };
    }

    /**
     * Command execution context
     */
    @Data
    public static class CommandExecutionContext {
        private Long serverId;
        private String serverName;
        private String command;
        private String aiGeneratedCommand;
        private String aiPrompt;
        private String output;
        private Integer exitCode;
        private Long duration;
        private String sessionId;
        private String userIp;
        private String userAgent;
        private Integer status;
    }

    /**
     * Audit query params
     */
    @Data
    public static class AuditQueryParams {
        private Long serverId;
        private Long userId;
        private Integer riskLevel;
        private Integer approvalStatus;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int pageNum = 1;
        private int pageSize = 20;
    }

    /**
     * Compliance statistics
     */
    @Data
    public static class ComplianceStats {
        private int totalCommands;
        private int approvedCount;
        private int rejectedCount;
        private int pendingCount;
        private int highRiskCount;
    }
}