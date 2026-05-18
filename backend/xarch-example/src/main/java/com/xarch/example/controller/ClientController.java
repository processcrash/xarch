package com.xarch.example.controller;

import com.xarch.example.entity.Client;
import com.xarch.example.service.ClientService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.entity.SelectIdsDTO;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Client controller for OAuth/sso client management
 */
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping
    @XarchLog(value = "Query client list", type = "QUERY")
    public ApiResult<PageResult<Client>> page(
            @RequestParam(required = false) String clientName,
            @RequestParam(required = false) String clientId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(clientService.page(clientName, clientId, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<Client> detail(@PathVariable Long id) {
        return ApiResult.ok(clientService.getById(id));
    }

    @PostMapping
    @XarchLog(value = "Create client", type = "CREATE")
    public ApiResult<Void> create(@RequestBody Client client) {
        clientService.create(client);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update client", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Client client) {
        client.setId(id);
        clientService.update(client);
        return ApiResult.ok();
    }

    @DeleteMapping
    @XarchLog(value = "Delete client", type = "DELETE")
    public ApiResult<Void> delete(@RequestBody SelectIdsDTO dto) {
        for (Long id : dto.getIds()) {
            clientService.delete(id);
        }
        return ApiResult.ok();
    }

    @GetMapping("/options")
    public ApiResult<List<Client>> options() {
        return ApiResult.ok(clientService.list());
    }
}