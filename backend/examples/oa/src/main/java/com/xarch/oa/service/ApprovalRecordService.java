package com.xarch.oa.service;

import com.xarch.oa.entity.ApprovalRecord;

import java.util.List;

/**
 * Approval record business interface.
 */
public interface ApprovalRecordService {

    /** History for a particular business row. */
    List<ApprovalRecord> history(String businessType, Long businessId);
}
