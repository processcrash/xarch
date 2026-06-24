package com.xarch.oa.service;

import com.xarch.oa.dto.ApprovalDTO;
import com.xarch.oa.dto.LeaveRequestDTO;
import com.xarch.oa.entity.LeaveRequest;
import com.xarch.starter.core.result.PageResult;

/**
 * Leave request business interface.
 */
public interface LeaveRequestService {

    PageResult<LeaveRequest> page(Long userId, String status, int pageNum, int pageSize);

    LeaveRequest getById(Long id);

    /** Create a new draft. */
    void create(LeaveRequestDTO dto, Long userId);

    /** Edit a draft. */
    void update(Long id, LeaveRequestDTO dto);

    /** Submit a draft to the workflow. */
    void submit(Long id, Long userId);

    /** Approver action (APPROVE / REJECT / TRANSFER). */
    void act(Long id, ApprovalDTO action);

    /** Applicant-initiated cancel. */
    void cancel(Long id, Long userId);

    /** List pending approvals for a user. */
    java.util.List<LeaveRequest> listPendingForApprover(Long approverId);
}
