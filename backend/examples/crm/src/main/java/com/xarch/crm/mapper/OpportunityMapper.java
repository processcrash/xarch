package com.xarch.crm.mapper;

import com.xarch.crm.entity.Opportunity;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Opportunity mapper.
 */
@Mapper
public interface OpportunityMapper extends BaseMapper<Opportunity> {

    /** Opportunities for a given customer. */
    List<Opportunity> selectByCustomerId(@Param("customerId") Long customerId);

    /** Opportunities in a given stage. */
    List<Opportunity> selectByStage(@Param("stage") String stage);

    /** Sum of amounts in a given stage (used by analytics). */
    BigDecimal sumAmountByStage(@Param("stage") String stage);
}
