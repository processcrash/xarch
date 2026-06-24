package com.xarch.oa.controller;

import com.xarch.oa.dto.ApprovalDTO;
import com.xarch.oa.dto.ExpenseReportDTO;
import com.xarch.oa.entity.ExpenseReport;
import com.xarch.oa.service.ExpenseReportService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * Expense report REST endpoints.
 */
@RestController
@RequestMapping("/api/expense-reports")
public class ExpenseReportController {

    @Autowired
    private ExpenseReportService expenseReportService;

    @GetMapping
    @XarchLog(value = "Query expense reports", type = "QUERY")
    public ApiResult<PageResult<ExpenseReport>> page(@RequestParam(required = false) Long userId,
                                                     @RequestParam(required = false) String status,
                                                     @RequestParam(defaultValue = "1") int pageNum,
                                                     @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(expenseReportService.page(userId, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<ExpenseReport> detail(@PathVariable Long id) {
        return ApiResult.ok(expenseReportService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create expense report", type = "CREATE")
    public ApiResult<Void> create(@RequestBody ExpenseReportDTO dto, @RequestParam Long userId) {
        expenseReportService.create(dto, userId);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update expense report", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody ExpenseReportDTO dto) {
        expenseReportService.update(id, dto);
        return ApiResult.ok();
    }

    @PutMapping("/{id}/submit")
    @XarchLog(value = "Submit expense report", type = "OPERATION")
    public ApiResult<Void> submit(@PathVariable Long id, @RequestParam Long userId) {
        expenseReportService.submit(id, userId);
        return ApiResult.ok();
    }

    @PutMapping("/{id}/act")
    @XarchLog(value = "Act on expense report", type = "OPERATION")
    public ApiResult<Void> act(@PathVariable Long id, @RequestBody ApprovalDTO action) {
        expenseReportService.act(id, action);
        return ApiResult.ok();
    }

    @PutMapping("/{id}/reimburse")
    @XarchLog(value = "Reimburse expense", type = "OPERATION")
    public ApiResult<Void> reimburse(@PathVariable Long id,
                                     @RequestParam(required = false) Long reimbursementDate) {
        expenseReportService.reimburse(id, reimbursementDate);
        return ApiResult.ok();
    }

    @GetMapping("/pending")
    public ApiResult<List<ExpenseReport>> pending(@RequestParam Long approverId) {
        return ApiResult.ok(expenseReportService.listPendingForApprover(approverId));
    }

    /**
     * Sum of approved amounts for a user. Useful for the dashboard
     * "this month's spend" widget.
     */
    @GetMapping("/approved-sum")
    public ApiResult<BigDecimal> approvedSum(@RequestParam Long userId) {
        return ApiResult.ok(expenseReportService.sumApprovedAmount(userId));
    }
}
