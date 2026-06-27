package com.xarch.example.message.service;

import com.xarch.example.message.entity.Client;
import com.xarch.starter.core.result.PageResult;

import java.util.List;

/** Client service contract. */
public interface ClientService {
    PageResult<Client> page(String clientName, String clientId, int pageNum, int pageSize);
    Client getById(Long id);
    void create(Client client);
    void update(Client client);
    void delete(Long id);
    List<Client> list();
}