package com.xarch.example.message.service.impl;

import com.xarch.example.message.entity.Client;
import com.xarch.example.message.service.ClientService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** Stub ClientService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    @Override public PageResult<Client> page(String n, String id, int p, int s) { return PageResult.empty(); }
    @Override public Client getById(Long id) { return null; }
    @Override public void create(Client c) { }
    @Override public void update(Client c) { }
    @Override public void delete(Long id) { }
    @Override public List<Client> list() { return List.of(); }
}