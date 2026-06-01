package com.xarch.example.service.ai;

import cn.dev33.satoken.stp.StpUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.entity.ai.CommandAudit;
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

        AiAgentService.SafetyValidation validation = aiAgentService.validateCommand(context.getCommand());
        audit.setRiskLevel(mapSafetyToRiskLevel(validation.getLevel()));
        audit.setApprovalStatus(0);

        if (audit.getRiskLevel() <= 1) {
            audit.setApprovalStatus(1);
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
            audit.setApprovalStatus(0);
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

        audit.setApprovalStatus(1);
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

        audit.setApprovalStatus(2);
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
        QueryWrapper wrapper = QueryWrapper.create().from("ai_command_audit")
                .where("approval_status = 0 AND del_flag = 0")
                .orderBy("create_time", false);

        Page<CommandAudit> page = auditMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    /**
     * Get audit logs with filters
     */
    public PageResult<CommandAudit> getAuditLogs(AuditQueryParams params) {
        QueryWrapper wrapper = QueryWrapper.create().from("ai_command_audit").where("del_flag = 0");

        if (params.getServerId() != null) {
            wrapper.and("server_id = ?", params.getServerId());
        }
        if (params.getUserId() != null) {
            wrapper.and("user_id = ?", params.getUserId());
        }
        if (params.getRiskLevel() != null) {
            wrapper.and("risk_level = ?", params.getRiskLevel());
        }
        if (params.getApprovalStatus() != null) {
            wrapper.and("approval_status = ?", params.getApprovalStatus());
        }
        if (params.getStartTime() != null) {
            wrapper.and("create_time >= ?", params.getStartTime());
        }
        if (params.getEndTime() != null) {
            wrapper.and("create_time <= ?", params.getEndTime());
        }
        wrapper.orderBy("create_time", false);

        Page<CommandAudit> page = auditMapper.paginate(params.getPageNum(), params.getPageSize(), wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
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
        QueryWrapper wrapper = QueryWrapper.create().from("ai_command_audit")
                .where("user_id = ? AND del_flag = 0", userId)
                .orderBy("create_time", false);

        Page<CommandAudit> page = auditMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    /**
     * Get compliance statistics
     */
    public ComplianceStats getComplianceStats(LocalDateTime start, LocalDateTime end) {
        QueryWrapper wrapper = QueryWrapper.create().from("ai_command_audit").where("del_flag = 0");
        if (start != null) wrapper.and("create_time >= ?", start);
        if (end != null) wrapper.and("create_time <= ?", end);

        List<CommandAudit> audits = auditMapper.selectListByQuery(wrapper);

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

    @Data
    public static class ComplianceStats {
        private int totalCommands;
        private int approvedCount;
        private int rejectedCount;
        private int pendingCount;
        private int highRiskCount;
    }
}