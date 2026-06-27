package com.xarch.crm.controller;

import com.xarch.crm.dto.ContractDTO;
import com.xarch.crm.entity.Contract;
import com.xarch.crm.service.ContractService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contract REST endpoints.
 */
@RestController
@RequestMapping("/api/contracts")
@Tag(name = "Contract")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @GetMapping
    @XarchLog(value = "Query contracts", type = "QUERY")
    public ApiResult<PageResult<Contract>> page(@RequestParam(required = false) Long customerId,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(defaultValue = "1") int pageNum,
                                                @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(contractService.page(customerId, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<Contract> detail(@PathVariable Long id) {
        return ApiResult.ok(contractService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create contract", type = "CREATE")
    public ApiResult<Void> create(@RequestBody ContractDTO dto) {
        contractService.create(dto);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update contract", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody ContractDTO dto) {
        contractService.update(id, dto);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete contract", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        contractService.delete(id);
        return ApiResult.ok();
    }

    /**
     * Terminate a contract. Sets {@code status=TERMINATED} and
     * stamps {@code signedDate} if it was not set before.
     */
    @PostMapping("/{id}/terminate")
    @XarchLog(value = "Terminate contract", type = "OPERATION")
    public ApiResult<Void> terminate(@PathVariable Long id) {
        contractService.terminate(id);
        return ApiResult.ok();
    }
}