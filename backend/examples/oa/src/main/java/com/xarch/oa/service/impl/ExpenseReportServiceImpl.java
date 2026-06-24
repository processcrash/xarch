package com.xarch.oa.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.oa.dto.ApprovalDTO;
import com.xarch.oa.dto.ExpenseReportDTO;
import com.xarch.oa.entity.ExpenseReport;
import com.xarch.oa.entity.Workflow;
import com.xarch.oa.exception.OaException;
import com.xarch.oa.mapper.ExpenseReportMapper;
import com.xarch.oa.service.ExpenseReportService;
import com.xarch.oa.workflow.WorkflowDefinition;
import com.xarch.oa.workflow.WorkflowEngine;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * Expense report service. Mirrors the leave request service shape.
 */
@Service
public class ExpenseReportServiceImpl implements ExpenseReportService {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_APPROVING = "APPROVING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_REIMBURSED = "REIMBURSED";

    @Autowired
    private ExpenseReportMapper expenseReportMapper;

    @Autowired
    private WorkflowEngine workflowEngine;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public PageResult<ExpenseReport> page(Long userId, String status, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from(ExpenseReport.class);
        if (userId != null) {
            wrapper.and("user_id = ?", userId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.and("status = ?", status);
        }
        wrapper.orderBy("create_time", false);
        Page<ExpenseReport> page = expenseReportMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    @Override
    public ExpenseReport getById(Long id) {
        return expenseReportMapper.selectOneById(id);
    }

    @Override
    public void create(ExpenseReportDTO dto, Long userId) {
        ExpenseReport report = ExpenseReport.builder()
                .userId(userId)
                .category(dto.category())
                .amount(dto.amount())
                .currency(dto.currency())
                .description(dto.description())
                .items(encodeItems(dto.items()))
                .status(STATUS_DRAFT)
                .build();
        expenseReportMapper.insert(report);
    }

    @Override
    public void update(Long id, ExpenseReportDTO dto) {
        ExpenseReport existing = require(id);
        if (!STATUS_DRAFT.equals(existing.getStatus())) {
            throw new OaException("Only drafts can be edited");
        }
        existing.setCategory(dto.category());
        existing.setAmount(dto.amount());
        existing.setCurrency(dto.currency());
        existing.setDescription(dto.description());
        existing.setItems(encodeItems(dto.items()));
        expenseReportMapper.update(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id, Long userId) {
        ExpenseReport existing = require(id);
        if (!STATUS_DRAFT.equals(existing.getStatus())) {
            throw new OaException("Only drafts can be submitted");
        }
        existing.setStatus(STATUS_SUBMITTED);
        expenseReportMapper.update(existing);

        Workflow workflow = workflowEngine.requireWorkflow("EXPENSE");
        workflow.setId(id);
        WorkflowDefinition.Node first = workflowEngine.start(workflow);
        existing.setStatus(STATUS_APPROVING);
        existing.setApproverId(first.approvers().isEmpty() ? null : first.approvers().get(0));
        expenseReportMapper.update(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void act(Long id, ApprovalDTO action) {
        ExpenseReport existing = require(id);
        if (!STATUS_APPROVING.equals(existing.getStatus())) {
            throw new OaException("Report is not awaiting approval");
        }
        Workflow workflow = workflowEngine.requireWorkflow("EXPENSE");
        workflow.setId(id);

        WorkflowDefinition.Node next = workflowEngine.act(workflow, action, record -> {
            record.setBusinessId(id);
        });

        if (next == null) {
            existing.setStatus("APPROVE".equals(action.action()) ? STATUS_APPROVED : STATUS_REJECTED);
            existing.setApproverId(null);
        } else {
            existing.setApproverId(
                    next.approvers() == null || next.approvers().isEmpty()
                            ? null
                            : next.approvers().get(0));
        }
        expenseReportMapper.update(existing);
    }

    @Override
    public void reimburse(Long id, Long reimbursementDate) {
        ExpenseReport existing = require(id);
        if (!STATUS_APPROVED.equals(existing.getStatus())) {
            throw new OaException("Only approved reports can be reimbursed");
        }
        existing.setStatus(STATUS_REIMBURSED);
        existing.setReimbursementDate(reimbursementDate == null
                ? System.currentTimeMillis()
                : reimbursementDate);
        expenseReportMapper.update(existing);
    }

    @Override
    public List<ExpenseReport> listPendingForApprover(Long approverId) {
        return expenseReportMapper.selectPendingForApprover(approverId);
    }

    @Override
    public BigDecimal sumApprovedAmount(Long userId) {
        BigDecimal sum = expenseReportMapper.sumApprovedAmount(userId);
        return sum == null ? BigDecimal.ZERO : sum;
    }

    private ExpenseReport require(Long id) {
        ExpenseReport existing = expenseReportMapper.selectOneById(id);
        if (existing == null) {
            throw new OaException("Expense report not found: " + id);
        }
        return existing;
    }

    private String encodeItems(List<com.xarch.oa.entity.ExpenseItem> items) {
        if (items == null) {
            return "[]";
        }
        try {
            return mapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            throw new OaException("Failed to encode items");
        }
    }
}
