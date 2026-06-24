package com.xarch.oa.controller;

import com.xarch.oa.dto.ApprovalDTO;
import com.xarch.oa.dto.LeaveRequestDTO;
import com.xarch.oa.entity.LeaveRequest;
import com.xarch.oa.service.LeaveRequestService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Leave request REST endpoints.
 */
@RestController
@RequestMapping("/api/leave-requests")
public class LeaveRequestController {

    @Autowired
    private LeaveRequestService leaveRequestService;

    @GetMapping
    @XarchLog(value = "Query leave requests", type = "QUERY")
    public ApiResult<PageResult<LeaveRequest>> page(@RequestParam(required = false) Long userId,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(leaveRequestService.page(userId, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<LeaveRequest> detail(@PathVariable Long id) {
        return ApiResult.ok(leaveRequestService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create leave request", type = "CREATE")
    public ApiResult<Void> create(@RequestBody LeaveRequestDTO dto, @RequestParam Long userId) {
        leaveRequestService.create(dto, userId);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update leave request", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody LeaveRequestDTO dto) {
        leaveRequestService.update(id, dto);
        return ApiResult.ok();
    }

    @PutMapping("/{id}/submit")
    @XarchLog(value = "Submit leave request", type = "OPERATION")
    public ApiResult<Void> submit(@PathVariable Long id, @RequestParam Long userId) {
        leaveRequestService.submit(id, userId);
        return ApiResult.ok();
    }

    @PutMapping("/{id}/cancel")
    @XarchLog(value = "Cancel leave request", type = "OPERATION")
    public ApiResult<Void> cancel(@PathVariable Long id, @RequestParam Long userId) {
        leaveRequestService.cancel(id, userId);
        return ApiResult.ok();
    }

    /**
     * Approver action endpoint. Same body for approve / reject / transfer.
     */
    @PutMapping("/{id}/act")
    @XarchLog(value = "Act on leave request", type = "OPERATION")
    public ApiResult<Void> act(@PathVariable Long id, @RequestBody ApprovalDTO action) {
        leaveRequestService.act(id, action);
        return ApiResult.ok();
    }

    /**
     * Pending approvals for a given approver.
     */
    @GetMapping("/pending")
    public ApiResult<List<LeaveRequest>> pending(@RequestParam Long approverId) {
        return ApiResult.ok(leaveRequestService.listPendingForApprover(approverId));
    }
}
