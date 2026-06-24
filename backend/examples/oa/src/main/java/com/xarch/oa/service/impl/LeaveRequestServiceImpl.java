package com.xarch.oa.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.oa.dto.ApprovalDTO;
import com.xarch.oa.dto.LeaveRequestDTO;
import com.xarch.oa.entity.ApprovalRecord;
import com.xarch.oa.entity.LeaveRequest;
import com.xarch.oa.entity.Workflow;
import com.xarch.oa.exception.OaException;
import com.xarch.oa.mapper.LeaveRequestMapper;
import com.xarch.oa.service.LeaveRequestService;
import com.xarch.oa.workflow.WorkflowDefinition;
import com.xarch.oa.workflow.WorkflowEngine;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Leave request service. Drives the workflow engine and exposes the
 * usual paginated / CRUD surface.
 */
@Service
public class LeaveRequestServiceImpl implements LeaveRequestService {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_SUBMITTED = "SUBMITTED";
    public static final String STATUS_APPROVING = "APPROVING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    @Autowired
    private LeaveRequestMapper leaveRequestMapper;

    @Autowired
    private WorkflowEngine workflowEngine;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public PageResult<LeaveRequest> page(Long userId, String status, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from(LeaveRequest.class);
        if (userId != null) {
            wrapper.and("user_id = ?", userId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.and("status = ?", status);
        }
        wrapper.orderBy("create_time", false);
        Page<LeaveRequest> page = leaveRequestMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    @Override
    public LeaveRequest getById(Long id) {
        return leaveRequestMapper.selectOneById(id);
    }

    @Override
    public void create(LeaveRequestDTO dto, Long userId) {
        LeaveRequest request = LeaveRequest.builder()
                .userId(userId)
                .type(dto.type())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .days(calculateDays(dto.startDate(), dto.endDate()))
                .reason(dto.reason())
                .status(STATUS_DRAFT)
                .attachments(encodeAttachments(dto.attachments()))
                .build();
        leaveRequestMapper.insert(request);
    }

    @Override
    public void update(Long id, LeaveRequestDTO dto) {
        LeaveRequest existing = require(id);
        if (!STATUS_DRAFT.equals(existing.getStatus())) {
            throw new OaException("Only drafts can be edited");
        }
        existing.setType(dto.type());
        existing.setStartDate(dto.startDate());
        existing.setEndDate(dto.endDate());
        existing.setDays(calculateDays(dto.startDate(), dto.endDate()));
        existing.setReason(dto.reason());
        existing.setAttachments(encodeAttachments(dto.attachments()));
        leaveRequestMapper.update(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long id, Long userId) {
        LeaveRequest existing = require(id);
        if (!STATUS_DRAFT.equals(existing.getStatus())) {
            throw new OaException("Only drafts can be submitted");
        }
        existing.setStatus(STATUS_SUBMITTED);
        leaveRequestMapper.update(existing);

        Workflow workflow = workflowEngine.requireWorkflow("LEAVE");
        // Bind this business row to the workflow by encoding the id into
        // a per-row workflow instance is overkill for the example, so
        // the engine just routes off business type + business id which
        // we plumb in via the approval record.
        workflow.setId(id);
        WorkflowDefinition.Node first = workflowEngine.start(workflow);
        existing.setStatus(STATUS_APPROVING);
        existing.setCurrentApproverId(first.approvers().isEmpty() ? null : first.approvers().get(0));
        leaveRequestMapper.update(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void act(Long id, ApprovalDTO action) {
        LeaveRequest existing = require(id);
        if (!STATUS_APPROVING.equals(existing.getStatus())) {
            throw new OaException("Request is not awaiting approval");
        }
        Workflow workflow = workflowEngine.requireWorkflow("LEAVE");
        workflow.setId(id);

        WorkflowDefinition.Node next = workflowEngine.act(workflow, action, record -> {
            // attach businessId after the engine has set the auto-id
            record.setBusinessId(id);
        });

        if (next == null) {
            existing.setStatus("APPROVE".equals(action.action()) ? STATUS_APPROVED : STATUS_REJECTED);
            existing.setCurrentApproverId(null);
        } else {
            existing.setCurrentApproverId(
                    next.approvers() == null || next.approvers().isEmpty()
                            ? null
                            : next.approvers().get(0));
        }
        leaveRequestMapper.update(existing);
    }

    @Override
    public void cancel(Long id, Long userId) {
        LeaveRequest existing = require(id);
        if (!existing.getUserId().equals(userId)) {
            throw new OaException("Only the applicant can cancel");
        }
        if (STATUS_APPROVED.equals(existing.getStatus())
                || STATUS_REJECTED.equals(existing.getStatus())
                || STATUS_CANCELLED.equals(existing.getStatus())) {
            throw new OaException("Request is already terminal");
        }
        existing.setStatus(STATUS_CANCELLED);
        existing.setCurrentApproverId(null);
        leaveRequestMapper.update(existing);
    }

    @Override
    public List<LeaveRequest> listPendingForApprover(Long approverId) {
        return leaveRequestMapper.selectPendingForApprover(approverId);
    }

    private LeaveRequest require(Long id) {
        LeaveRequest existing = leaveRequestMapper.selectOneById(id);
        if (existing == null) {
            throw new OaException("Leave request not found: " + id);
        }
        return existing;
    }

    private double calculateDays(Long startDate, Long endDate) {
        if (startDate == null || endDate == null) {
            return 0d;
        }
        long diff = endDate - startDate;
        if (diff < 0) {
            return 0d;
        }
        return Math.max(1d, Math.ceil(diff / 86_400_000d));
    }

    private String encodeAttachments(List<Long> attachments) {
        if (attachments == null) {
            return "[]";
        }
        try {
            return mapper.writeValueAsString(attachments);
        } catch (JsonProcessingException e) {
            throw new OaException("Failed to encode attachments");
        }
    }
}
