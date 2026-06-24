package com.xarch.oa.service.impl;

import com.xarch.oa.entity.ApprovalRecord;
import com.xarch.oa.mapper.ApprovalRecordMapper;
import com.xarch.oa.service.ApprovalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Approval record service implementation.
 */
@Service
public class ApprovalRecordServiceImpl implements ApprovalRecordService {

    @Autowired
    private ApprovalRecordMapper approvalRecordMapper;

    @Override
    public List<ApprovalRecord> history(String businessType, Long businessId) {
        return approvalRecordMapper.selectByBusiness(businessType, businessId);
    }
}
