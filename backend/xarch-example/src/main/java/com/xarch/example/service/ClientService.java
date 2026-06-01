package com.xarch.example.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
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
        QueryWrapper wrapper = QueryWrapper.create().from("sys_client").where("del_flag = 0");
        if (StringUtils.hasText(clientName)) {
            wrapper.and("client_name LIKE ?", "%" + clientName + "%");
        }
        if (StringUtils.hasText(clientId)) {
            wrapper.and("client_id = ?", clientId);
        }
        wrapper.orderBy("create_time", false);

        Page<Client> page = clientMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    public Client getById(Long id) {
        return clientMapper.selectById(id);
    }

    public List<Client> list() {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_client").where("del_flag = 0");
        return clientMapper.selectListByQuery(wrapper);
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