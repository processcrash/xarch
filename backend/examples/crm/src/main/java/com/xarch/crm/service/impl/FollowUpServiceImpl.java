package com.xarch.crm.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.crm.dto.FollowUpDTO;
import com.xarch.crm.entity.Customer;
import com.xarch.crm.entity.FollowUp;
import com.xarch.crm.exception.CrmException;
import com.xarch.crm.mapper.CustomerMapper;
import com.xarch.crm.mapper.FollowUpMapper;
import com.xarch.crm.service.FollowUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Follow-up service implementation. Logs interactions and stamps the
 * customer's {@code lastContactTime} when one is recorded.
 */
@Service
public class FollowUpServiceImpl implements FollowUpService {

    @Autowired
    private FollowUpMapper followUpMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(FollowUpDTO dto) {
        long now = System.currentTimeMillis();
        FollowUp followUp = FollowUp.builder()
                .customerId(dto.customerId())
                .contactId(dto.contactId())
                .opportunityId(dto.opportunityId())
                .type(dto.type())
                .content(dto.content())
                .result(dto.result())
                .nextFollowUpDate(dto.nextFollowUpDate())
                .userId(dto.userId())
                .createTime(now)
                .isDeleted(0)
                .build();
        followUpMapper.insert(followUp);

        // stamp customer lastContactTime
        Customer customer = customerMapper.selectOneById(dto.customerId());
        if (customer != null) {
            customer.setLastContactTime(now);
            customerMapper.update(customer);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        FollowUp followUp = followUpMapper.selectOneById(id);
        if (followUp == null) {
            throw new CrmException("FollowUp not found: " + id);
        }
        followUp.setIsDeleted(1);
        followUpMapper.update(followUp);
    }

    @Override
    public List<FollowUp> listByCustomer(Long customerId) {
        return followUpMapper.selectByCustomerId(customerId);
    }

    /**
     * Follow-ups linked to a specific opportunity.
     */
    public List<FollowUp> listByOpportunity(Long opportunityId) {
        return followUpMapper.selectListByQuery(QueryWrapper.create()
                .from(FollowUp.class)
                .where("opportunity_id = ?", opportunityId)
                .orderBy("create_time", false));
    }

    /**
     * Find follow-ups whose {@code nextFollowUpDate} is on or before the
     * given cutoff (epoch millis). Returns empty list when {@code date}
     * is null.
     */
    public List<FollowUp> scheduleNext(Long date) {
        if (date == null) {
            return List.of();
        }
        return followUpMapper.selectListByQuery(QueryWrapper.create()
                .from(FollowUp.class)
                .where("next_follow_up_date IS NOT NULL")
                .and("next_follow_up_date <= ?", date)
                .orderBy("next_follow_up_date", true));
    }

    @Override
    public FollowUp getById(Long id) {
        return followUpMapper.selectOneById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, FollowUpDTO dto) {
        FollowUp existing = followUpMapper.selectOneById(id);
        if (existing == null) {
            throw new CrmException("FollowUp not found: " + id);
        }
        existing.setType(dto.type());
        existing.setContent(dto.content());
        existing.setResult(dto.result());
        existing.setNextFollowUpDate(dto.nextFollowUpDate());
        existing.setContactId(dto.contactId());
        existing.setOpportunityId(dto.opportunityId());
        existing.setUserId(dto.userId());
        followUpMapper.update(existing);
    }
}