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

    void update(Long id, FollowUpDTO dto);

    FollowUp getById(Long id);

    List<FollowUp> listByCustomer(Long customerId);

    List<FollowUp> listByOpportunity(Long opportunityId);

    /** Find follow-ups whose {@code nextFollowUpDate} is on or before the cutoff. */
    List<FollowUp> scheduleNext(Long date);
}