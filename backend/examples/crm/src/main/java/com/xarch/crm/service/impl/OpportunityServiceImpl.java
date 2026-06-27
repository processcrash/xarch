package com.xarch.crm.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.crm.dto.OpportunityDTO;
import com.xarch.crm.entity.Opportunity;
import com.xarch.crm.exception.CrmException;
import com.xarch.crm.mapper.OpportunityMapper;
import com.xarch.crm.service.OpportunityService;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Opportunity service implementation. Manages the funnel transitions
 * QUALIFICATION -> NEEDS_ANALYSIS -> PROPOSAL -> NEGOTIATION -> WON / LOST
 * and the probability / status that follows from those transitions.
 */
@Service
public class OpportunityServiceImpl implements OpportunityService {

    public static final String STAGE_QUALIFICATION = "QUALIFICATION";
    public static final String STAGE_NEEDS_ANALYSIS = "NEEDS_ANALYSIS";
    public static final String STAGE_PROPOSAL = "PROPOSAL";
    public static final String STAGE_NEGOTIATION = "NEGOTIATION";
    public static final String STAGE_WON = "WON";
    public static final String STAGE_LOST = "LOST";

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_WON = "WON";
    public static final String STATUS_LOST = "LOST";

    @Autowired
    private OpportunityMapper opportunityMapper;

    @Override
    public PageResult<Opportunity> page(String name, String stage, Long customerId, Long ownerId,
                                        int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from(Opportunity.class);
        if (StringUtils.hasText(name)) {
            wrapper.and("name LIKE ?", "%" + name + "%");
        }
        if (StringUtils.hasText(stage)) {
            wrapper.and("stage = ?", stage);
        }
        if (customerId != null) {
            wrapper.and("customer_id = ?", customerId);
        }
        if (ownerId != null) {
            wrapper.and("owner_id = ?", ownerId);
        }
        wrapper.orderBy("create_time", false);
        Page<Opportunity> page = opportunityMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    @Override
    public Opportunity getById(Long id) {
        return opportunityMapper.selectOneById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(OpportunityDTO dto) {
        long now = System.currentTimeMillis();
        Opportunity opportunity = Opportunity.builder()
                .customerId(dto.customerId())
                .name(dto.name())
                .amount(dto.amount())
                .currency(StringUtils.hasText(dto.currency()) ? dto.currency() : "CNY")
                .stage(dto.stage())
                .probability(dto.probability() == null ? 10 : dto.probability())
                .expectedCloseDate(dto.expectedCloseDate())
                .ownerId(dto.ownerId())
                .description(dto.description())
                .status(STATUS_OPEN)
                .createTime(now)
                .updateTime(now)
                .isDeleted(0)
                .build();
        opportunityMapper.insert(opportunity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, OpportunityDTO dto) {
        Opportunity opportunity = requireOpportunity(id);
        opportunity.setName(dto.name());
        opportunity.setAmount(dto.amount());
        opportunity.setCurrency(dto.currency());
        opportunity.setStage(dto.stage());
        opportunity.setProbability(dto.probability());
        opportunity.setExpectedCloseDate(dto.expectedCloseDate());
        opportunity.setOwnerId(dto.ownerId());
        opportunity.setDescription(dto.description());
        opportunity.setUpdateTime(System.currentTimeMillis());
        opportunityMapper.update(opportunity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStage(Long id, String stage) {
        Opportunity opportunity = requireOpportunity(id);
        opportunity.setStage(stage);
        opportunity.setUpdateTime(System.currentTimeMillis());
        if (STAGE_WON.equals(stage)) {
            markWon(id);
            return;
        }
        if (STAGE_LOST.equals(stage)) {
            markLost(id);
            return;
        }
        opportunityMapper.update(opportunity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markWon(Long id) {
        Opportunity opportunity = requireOpportunity(id);
        opportunity.setStage(STAGE_WON);
        opportunity.setStatus(STATUS_WON);
        opportunity.setProbability(100);
        opportunity.setUpdateTime(System.currentTimeMillis());
        opportunityMapper.update(opportunity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markLost(Long id) {
        Opportunity opportunity = requireOpportunity(id);
        opportunity.setStage(STAGE_LOST);
        opportunity.setStatus(STATUS_LOST);
        opportunity.setProbability(0);
        opportunity.setUpdateTime(System.currentTimeMillis());
        opportunityMapper.update(opportunity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Opportunity opportunity = requireOpportunity(id);
        opportunity.setIsDeleted(1);
        opportunity.setUpdateTime(System.currentTimeMillis());
        opportunityMapper.update(opportunity);
    }

    @Override
    public List<Opportunity> listByCustomer(Long customerId) {
        return opportunityMapper.selectByCustomerId(customerId);
    }

    /**
     * Look up an opportunity or throw a domain exception.
     */
    private Opportunity requireOpportunity(Long id) {
        Opportunity opportunity = opportunityMapper.selectOneById(id);
        if (opportunity == null) {
            throw new CrmException("Opportunity not found: " + id);
        }
        return opportunity;
    }
}