package com.xarch.crm.mapper;

import com.xarch.crm.entity.Contract;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * Contract mapper.
 */
@Mapper
public interface ContractMapper extends BaseMapper<Contract> {

    /** Contracts for a customer. */
    List<Contract> selectByCustomerId(@Param("customerId") Long customerId);

    /** Sum of active contract amounts for a customer. */
    BigDecimal sumActiveAmountByCustomer(@Param("customerId") Long customerId);
}
