package com.xarch.oa.mapper;

import com.xarch.oa.entity.ApprovalRecord;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Approval record mapper. Provides a unified "approvals on business X"
 * query regardless of business type.
 */
@Mapper
public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {

    /**
     * Records for a particular business row, ordered oldest first so the
     * timeline reads top-down in the UI.
     */
    List<ApprovalRecord> selectByBusiness(@Param("businessType") String businessType,
                                          @Param("businessId") Long businessId);
}
