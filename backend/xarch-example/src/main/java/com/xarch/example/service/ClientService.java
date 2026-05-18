package com.xarch.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.Client;
import com.xarch.example.mapper.ClientMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Client service
 */
@Service
public class ClientService {

    @Autowired
    private ClientMapper clientMapper;

    public PageResult<Client> page(String clientName, String clientId, int pageNum, int pageSize) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Client>();
        if (StringUtils.hasText(clientName)) {
            wrapper.like(Client::getClientName, clientName);
        }
        if (StringUtils.hasText(clientId)) {
            wrapper.eq(Client::getClientId, clientId);
        }
        wrapper.orderByDesc(Client::getCreateTime);

        Page<Client> page = new Page<>(pageNum, pageSize);
        Page<Client> result = clientMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    public Client getById(Long id) {
        return clientMapper.selectById(id);
    }

    public List<Client> list() {
        return clientMapper.selectList(null);
    }

    public void create(Client client) {
        clientMapper.insert(client);
    }

    public void update(Client client) {
        clientMapper.updateById(client);
    }

    public void delete(Long id) {
        clientMapper.deleteById(id);
    }
}