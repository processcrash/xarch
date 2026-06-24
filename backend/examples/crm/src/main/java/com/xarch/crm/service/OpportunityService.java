package com.xarch.crm.service;

import com.xarch.crm.dto.OpportunityDTO;
import com.xarch.crm.entity.Opportunity;
import com.xarch.starter.core.result.PageResult;

import java.util.List;

/**
 * Opportunity business interface.
 */
public interface OpportunityService {

    PageResult<Opportunity> page(String name, String stage, Long customerId, Long ownerId,
                                 int pageNum, int pageSize);

    Opportunity getById(Long id);

    void create(OpportunityDTO dto);

    void update(Long id, OpportunityDTO dto);

    /** Move an opportunity to a new stage. */
    void changeStage(Long id, String stage);

    void markWon(Long id);

    void markLost(Long id);

    List<Opportunity> listByCustomer(Long customerId);
}
