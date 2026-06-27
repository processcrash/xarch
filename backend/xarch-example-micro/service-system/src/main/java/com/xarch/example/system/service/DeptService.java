package com.xarch.example.system.service;

import com.xarch.example.system.entity.Dept;
import com.xarch.starter.core.result.PageResult;

import java.util.List;

/** Dept service contract. */
public interface DeptService {
    PageResult<Dept> page(String deptName, int pageNum, int pageSize);
    List<Dept> tree();
    Dept getById(Long id);
    void create(Dept dept);
    void update(Dept dept);
    void delete(Long id);
}