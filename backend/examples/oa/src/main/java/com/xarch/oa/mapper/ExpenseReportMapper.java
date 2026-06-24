package com.xarch.oa.mapper;

import com.xarch.oa.entity.ExpenseReport;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Expense report mapper.
 */
@Mapper
public interface ExpenseReportMapper extends BaseMapper<ExpenseReport> {

    /** Pending reports for the given approver. */
    List<ExpenseReport> selectPendingForApprover(@Param("approverId") Long approverId);

    /** Sum of approved amounts for a user. Used by the analytics panel. */
    BigDecimal sumApprovedAmount(@Param("userId") Long userId);
}
