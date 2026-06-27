package com.xarch.example.ai.service.impl;

import com.xarch.example.ai.entity.CommandAudit;
import com.xarch.example.ai.service.CommandAuditService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/** Stub CommandAuditService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommandAuditServiceImpl implements CommandAuditService {
    @Override public PageResult<CommandAudit> getAuditLogs(AuditQueryParams p) { return PageResult.empty(); }
    @Override public CommandAudit getById(Long id) { return null; }
    @Override public PageResult<CommandAudit> getPendingApprovals(int p, int s) { return PageResult.empty(); }
    @Override public boolean approve(Long id, String c) { return false; }
    @Override public boolean reject(Long id, String r) { return false; }
    @Override public ComplianceStats getComplianceStats(LocalDateTime s, LocalDateTime e) { return new ComplianceStats(); }
    @Override public PageResult<CommandAudit> getUserHistory(Long uid, int p, int s) { return PageResult.empty(); }
}