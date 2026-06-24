package com.xarch.crm.mapper;

import com.xarch.crm.entity.Contact;
import com.xarch.starter.db.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Contact mapper.
 */
@Mapper
public interface ContactMapper extends BaseMapper<Contact> {

    /** All contacts for a customer. */
    List<Contact> selectByCustomerId(@Param("customerId") Long customerId);

    /** The single primary contact for a customer, if any. */
    Contact selectPrimaryByCustomerId(@Param("customerId") Long customerId);
}
