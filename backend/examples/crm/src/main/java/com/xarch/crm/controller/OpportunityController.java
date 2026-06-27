package com.xarch.crm.controller;

import com.xarch.crm.dto.OpportunityDTO;
import com.xarch.crm.entity.Opportunity;
import com.xarch.crm.service.OpportunityService;
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

import java.util.List;
import java.util.Map;

/**
 * Opportunity REST endpoints.
 */
@RestController
@RequestMapping("/api/opportunities")
@Tag(name = "Opportunity")
public class OpportunityController {

    @Autowired
    private OpportunityService opportunityService;

    @GetMapping
    @XarchLog(value = "Query opportunities", type = "QUERY")
    public ApiResult<PageResult<Opportunity>> page(@RequestParam(required = false) String name,
                                                   @RequestParam(required = false) String stage,
                                                   @RequestParam(required = false) Long customerId,
                                                   @RequestParam(required = false) Long ownerId,
                                                   @RequestParam(defaultValue = "1") int pageNum,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(opportunityService.page(name, stage, customerId, ownerId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<Opportunity> detail(@PathVariable Long id) {
        return ApiResult.ok(opportunityService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create opportunity", type = "CREATE")
    public ApiResult<Void> create(@RequestBody OpportunityDTO dto) {
        opportunityService.create(dto);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update opportunity", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody OpportunityDTO dto) {
        opportunityService.update(id, dto);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete opportunity", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        opportunityService.delete(id);
        return ApiResult.ok();
    }

    /**
     * Move to a new funnel stage. Body: {@code {"stage":"WON"}}.
     */
    @PostMapping("/{id}/stage")
    @XarchLog(value = "Change stage", type = "OPERATION")
    public ApiResult<Void> changeStage(@PathVariable Long id, @RequestBody Map<String, String> body) {
        opportunityService.changeStage(id, body.get("stage"));
        return ApiResult.ok();
    }

    /**
     * Opportunities for a single customer (used by the customer detail page).
     */
    @GetMapping("/by-customer/{customerId}")
    public ApiResult<List<Opportunity>> listByCustomer(@PathVariable Long customerId) {
        return ApiResult.ok(opportunityService.listByCustomer(customerId));
    }
}