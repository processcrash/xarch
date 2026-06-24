package com.xarch.oa.mapper;

import com.xarch.oa.entity.LeaveRequest;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Leave request mapper. Custom queries stay here; CRUD is inherited.
 */
@Mapper
public interface LeaveRequestMapper extends BaseMapper<LeaveRequest> {

    /** Leave requests where the given user is the current approver. */
    List<LeaveRequest> selectPendingForApprover(@Param("approverId") Long approverId);

    /** All leave requests for a given user, newest first. */
    List<LeaveRequest> selectByUserId(@Param("userId") Long userId);
}
