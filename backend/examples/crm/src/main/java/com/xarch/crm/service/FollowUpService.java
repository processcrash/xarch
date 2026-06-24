package com.xarch.crm.service;

import com.xarch.crm.dto.FollowUpDTO;
import com.xarch.crm.entity.FollowUp;

import java.util.List;

/**
 * Follow-up business interface.
 */
public interface FollowUpService {

    void create(FollowUpDTO dto);

    void delete(Long id);

    List<FollowUp> listByCustomer(Long customerId);
}
