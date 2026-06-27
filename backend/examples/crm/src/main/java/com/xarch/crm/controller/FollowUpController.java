package com.xarch.crm.controller;

import com.xarch.crm.dto.FollowUpDTO;
import com.xarch.crm.entity.FollowUp;
import com.xarch.crm.service.FollowUpService;
import com.xarch.crm.service.impl.FollowUpServiceImpl;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
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

/**
 * Follow-up REST endpoints. Logs interactions and surfaces
 * the "schedule next" view for reps.
 */
@RestController
@RequestMapping("/api/follow-ups")
@Tag(name = "FollowUp")
public class FollowUpController {

    @Autowired
    private FollowUpService followUpService;

    @Autowired
    private FollowUpServiceImpl followUpServiceImpl;

    /**
     * List by customer or opportunity. One of the two query params
     * must be provided.
     */
    @GetMapping
    public ApiResult<List<FollowUp>> list(@RequestParam(required = false) Long customerId,
                                          @RequestParam(required = false) Long opportunityId) {
        if (opportunityId != null) {
            return ApiResult.ok(followUpService.listByOpportunity(opportunityId));
        }
        if (customerId != null) {
            return ApiResult.ok(followUpService.listByCustomer(customerId));
        }
        return ApiResult.ok(List.of());
    }

    @GetMapping("/{id}")
    public ApiResult<FollowUp> detail(@PathVariable Long id) {
        return ApiResult.ok(followUpService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create follow-up", type = "CREATE")
    public ApiResult<Void> create(@RequestBody FollowUpDTO dto) {
        followUpService.create(dto);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update follow-up", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody FollowUpDTO dto) {
        followUpService.update(id, dto);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete follow-up", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        followUpService.delete(id);
        return ApiResult.ok();
    }

    /**
     * Find follow-ups whose {@code nextFollowUpDate} is on or before the
     * given cutoff (epoch millis).
     */
    @GetMapping("/schedule")
    public ApiResult<List<FollowUp>> schedule(@RequestParam Long date) {
        return ApiResult.ok(followUpServiceImpl.scheduleNext(date));
    }
}