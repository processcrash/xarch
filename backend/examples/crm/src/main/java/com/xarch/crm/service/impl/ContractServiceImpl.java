package com.xarch.crm.service.impl;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.crm.dto.ContractDTO;
import com.xarch.crm.entity.Contract;
import com.xarch.crm.exception.CrmException;
import com.xarch.crm.mapper.ContractMapper;
import com.xarch.crm.service.ContractService;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Contract service implementation. Validates contract number uniqueness
 * and supports the DRAFT -> ACTIVE -> TERMINATED lifecycle.
 */
@Service
public class ContractServiceImpl implements ContractService {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_EXPIRED = "EXPIRED";
    public static final String STATUS_TERMINATED = "TERMINATED";

    @Autowired
    private ContractMapper contractMapper;

    @Override
    public PageResult<Contract> page(Long customerId, String status, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from(Contract.class);
        if (customerId != null) {
            wrapper.and("customer_id = ?", customerId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.and("status = ?", status);
        }
        wrapper.orderBy("create_time", false);
        Page<Contract> page = contractMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    @Override
    public Contract getById(Long id) {
        return contractMapper.selectOneById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ContractDTO dto) {
        if (StringUtils.hasText(dto.contractNo())) {
            Contract existing = contractMapper.selectOneByQuery(QueryWrapper.create()
                    .from(Contract.class)
                    .where("contract_no = ?", dto.contractNo()));
            if (existing != null) {
                throw new CrmException("Contract number already exists: " + dto.contractNo());
            }
        }
        long now = System.currentTimeMillis();
        Contract contract = Contract.builder()
                .customerId(dto.customerId())
                .opportunityId(dto.opportunityId())
                .contractNo(dto.contractNo())
                .amount(dto.amount())
                .startDate(dto.startDate())
                .endDate(dto.endDate())
                .paymentTerms(dto.paymentTerms())
                .status(STATUS_DRAFT)
                .createTime(now)
                .updateTime(now)
                .isDeleted(0)
                .build();
        contractMapper.insert(contract);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ContractDTO dto) {
        Contract contract = requireContract(id);
        contract.setCustomerId(dto.customerId());
        contract.setOpportunityId(dto.opportunityId());
        contract.setContractNo(dto.contractNo());
        contract.setAmount(dto.amount());
        contract.setStartDate(dto.startDate());
        contract.setEndDate(dto.endDate());
        contract.setPaymentTerms(dto.paymentTerms());
        contract.setUpdateTime(System.currentTimeMillis());
        contractMapper.update(contract);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sign(Long id, Long signedDate) {
        Contract contract = requireContract(id);
        contract.setStatus(STATUS_ACTIVE);
        contract.setSignedDate(signedDate == null ? System.currentTimeMillis() : signedDate);
        contract.setUpdateTime(System.currentTimeMillis());
        contractMapper.update(contract);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void terminate(Long id) {
        Contract contract = requireContract(id);
        contract.setStatus(STATUS_TERMINATED);
        if (contract.getSignedDate() == null) {
            contract.setSignedDate(System.currentTimeMillis());
        }
        contract.setUpdateTime(System.currentTimeMillis());
        contractMapper.update(contract);
    }

    /**
     * Soft delete - sets isDeleted=1.
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Contract contract = requireContract(id);
        contract.setIsDeleted(1);
        contract.setUpdateTime(System.currentTimeMillis());
        contractMapper.update(contract);
    }

    /**
     * Look up a contract or throw a domain exception.
     */
    private Contract requireContract(Long id) {
        Contract contract = contractMapper.selectOneById(id);
        if (contract == null) {
            throw new CrmException("Contract not found: " + id);
        }
        return contract;
    }

    /**
     * Helper exposed for callers that need to aggregate contract value.
     */
    @SuppressWarnings("unused")
    private List<Contract> listByCustomer(Long customerId) {
        return contractMapper.selectByCustomerId(customerId);
    }
}