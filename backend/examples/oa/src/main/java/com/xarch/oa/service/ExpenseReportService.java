package com.xarch.oa.service;

import com.xarch.oa.dto.ApprovalDTO;
import com.xarch.oa.dto.ExpenseReportDTO;
import com.xarch.oa.entity.ExpenseReport;
import com.xarch.starter.core.result.PageResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * Expense report business interface.
 */
public interface ExpenseReportService {

    PageResult<ExpenseReport> page(Long userId, String status, int pageNum, int pageSize);

    ExpenseReport getById(Long id);

    /** Create a new draft. */
    void create(ExpenseReportDTO dto, Long userId);

    /** Edit a draft. */
    void update(Long id, ExpenseReportDTO dto);

    /** Submit a draft to the workflow. */
    void submit(Long id, Long userId);

    /** Approver action. */
    void act(Long id, ApprovalDTO action);

    /** Mark a report as reimbursed. */
    void reimburse(Long id, Long reimbursementDate);

    List<ExpenseReport> listPendingForApprover(Long approverId);

    /** Sum of approved amounts for a user. */
    BigDecimal sumApprovedAmount(Long userId);
}
