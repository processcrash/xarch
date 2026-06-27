package com.xarch.crm.service;

import com.xarch.crm.dto.ContractDTO;
import com.xarch.crm.entity.Contract;
import com.xarch.starter.core.result.PageResult;

/**
 * Contract business interface.
 */
public interface ContractService {

    PageResult<Contract> page(Long customerId, String status, int pageNum, int pageSize);

    Contract getById(Long id);

    void create(ContractDTO dto);

    void update(Long id, ContractDTO dto);

    /** Soft delete. */
    void delete(Long id);

    /** Mark a draft contract as signed and active. */
    void sign(Long id, Long signedDate);

    void terminate(Long id);
}