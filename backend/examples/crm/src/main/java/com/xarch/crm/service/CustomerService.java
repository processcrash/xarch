package com.xarch.crm.service;

import com.xarch.crm.dto.CustomerDTO;
import com.xarch.crm.entity.Customer;
import com.xarch.crm.entity.FollowUp;
import com.xarch.starter.core.result.PageResult;

import java.util.List;

/**
 * Customer business interface.
 */
public interface CustomerService {

    PageResult<Customer> page(String name, String type, String level, Long ownerId,
                              int pageNum, int pageSize);

    Customer getById(Long id);

    void create(CustomerDTO dto);

    void update(Long id, CustomerDTO dto);

    /** Soft delete. */
    void delete(Long id);

    /** Promote a customer to {@code CUSTOMER} (was LEAD / PROSPECT). */
    void convert(Long id);

    /** Mark a customer as {@code LOST} and stamp last contact time. */
    void lose(Long id, String reason);

    /** Record a follow-up and stamp {@code lastContactTime}. */
    void recordFollowUp(Long id, FollowUp followUp);
}
