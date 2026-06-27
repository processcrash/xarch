package com.xarch.example.system.service.impl;

import com.xarch.example.system.entity.Dept;
import com.xarch.example.system.service.DeptService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** Stub DeptService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {
    @Override public PageResult<Dept> page(String n, int p, int s) { return PageResult.empty(); }
    @Override public List<Dept> tree() { return List.of(); }
    @Override public Dept getById(Long id) { return null; }
    @Override public void create(Dept dept) { }
    @Override public void update(Dept dept) { }
    @Override public void delete(Long id) { }
}