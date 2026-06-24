package com.xarch.crm.mapper;

import com.xarch.crm.entity.Customer;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Customer mapper. Custom queries stay here; CRUD is inherited.
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {

    /**
     * List customers owned by a user.
     */
    List<Customer> selectByOwner(@Param("ownerId") Long ownerId);

    /**
     * List customers by type (LEAD / PROSPECT / CUSTOMER / LOST).
     */
    List<Customer> selectByType(@Param("type") String type);
}
