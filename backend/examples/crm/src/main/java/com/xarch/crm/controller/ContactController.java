package com.xarch.crm.controller;

import com.xarch.crm.dto.ContactDTO;
import com.xarch.crm.entity.Contact;
import com.xarch.crm.service.ContactService;
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
 * Contact REST endpoints.
 */
@RestController
@RequestMapping("/api/contacts")
@Tag(name = "Contact")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping
    public ApiResult<List<Contact>> listByCustomer(@RequestParam Long customerId) {
        return ApiResult.ok(contactService.listByCustomer(customerId));
    }

    @GetMapping("/{id}")
    public ApiResult<Contact> detail(@PathVariable Long id) {
        return ApiResult.ok(contactService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create contact", type = "CREATE")
    public ApiResult<Void> create(@RequestBody ContactDTO dto) {
        contactService.create(dto);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update contact", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody ContactDTO dto) {
        contactService.update(new ContactDTO(
                id,
                dto.customerId(),
                dto.name(),
                dto.position(),
                dto.phone(),
                dto.email(),
                dto.isPrimary()));
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete contact", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        contactService.delete(id);
        return ApiResult.ok();
    }

    /**
     * Promote a contact to the primary contact for its customer.
     */
    @PostMapping("/{id}/primary")
    @XarchLog(value = "Set primary contact", type = "OPERATION")
    public ApiResult<Void> setPrimary(@PathVariable Long id) {
        contactService.setPrimary(id);
        return ApiResult.ok();
    }
}