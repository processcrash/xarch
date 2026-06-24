package com.xarch.crm.mapper;

import com.xarch.crm.entity.FollowUp;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Follow-up mapper.
 */
@Mapper
public interface FollowUpMapper extends BaseMapper<FollowUp> {

    /** All follow-ups for a customer, newest first. */
    List<FollowUp> selectByCustomerId(@Param("customerId") Long customerId);
}
