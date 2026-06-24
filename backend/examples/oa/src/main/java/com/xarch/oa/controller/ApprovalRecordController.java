package com.xarch.oa.controller;

import com.xarch.oa.entity.ApprovalRecord;
import com.xarch.oa.service.ApprovalRecordService;
import com.xarch.starter.core.result.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Approval record REST endpoints. Generic over business type.
 */
@RestController
@RequestMapping("/api/approvals")
public class ApprovalRecordController {

    @Autowired
    private ApprovalRecordService approvalRecordService;

    @GetMapping("/history")
    public ApiResult<List<ApprovalRecord>> history(@RequestParam String businessType,
                                                   @RequestParam Long businessId) {
        return ApiResult.ok(approvalRecordService.history(businessType, businessId));
    }
}
