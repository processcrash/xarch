package com.xarch.crm.controller;

import com.xarch.crm.dto.CustomerDTO;
import com.xarch.crm.entity.Customer;
import com.xarch.crm.service.CustomerService;
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

import java.util.Map;

/**
 * Customer REST endpoints. Paginated search, lifecycle transitions
 * (assign / convert / lose) and standard CRUD.
 */
@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * Paginated search by name / type / level / ownerId.
     */
    @GetMapping
    @XarchLog(value = "Query customers", type = "QUERY")
    public ApiResult<PageResult<Customer>> page(@RequestParam(required = false) String name,
                                               @RequestParam(required = false) String type,
                                               @RequestParam(required = false) String level,
                                               @RequestParam(required = false) Long ownerId,
                                               @RequestParam(defaultValue = "1") int pageNum,
                                               @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(customerService.page(name, type, level, ownerId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<Customer> detail(@PathVariable Long id) {
        return ApiResult.ok(customerService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create customer", type = "CREATE")
    public ApiResult<Void> create(@RequestBody CustomerDTO dto) {
        customerService.create(dto);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update customer", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody CustomerDTO dto) {
        customerService.update(id, dto);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete customer", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ApiResult.ok();
    }

    /**
     * Reassign the sales owner. Body: {@code {"ownerId": 42}}.
     */
    @PostMapping("/{id}/assign")
    @XarchLog(value = "Assign owner", type = "OPERATION")
    public ApiResult<Void> assign(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        customerService.assignOwner(id, body.get("ownerId"));
        return ApiResult.ok();
    }

    /**
     * Promote a customer to {@code CUSTOMER}.
     */
    @PostMapping("/{id}/convert")
    @XarchLog(value = "Convert customer", type = "OPERATION")
    public ApiResult<Void> convert(@PathVariable Long id) {
        customerService.convert(id);
        return ApiResult.ok();
    }

    /**
     * Mark a customer as {@code LOST}. Body: {@code {"reason": "..."}}.
     */
    @PostMapping("/{id}/lose")
    @XarchLog(value = "Mark customer lost", type = "OPERATION")
    public ApiResult<Void> lose(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        customerService.lose(id, reason);
        return ApiResult.ok();
    }
}