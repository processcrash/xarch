package com.xarch.example.system.service.impl;

import com.xarch.example.system.entity.Dict;
import com.xarch.example.system.entity.DictData;
import com.xarch.example.system.service.DictService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/** Stub DictService impl. */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {
    @Override public PageResult<Dict> page(String n, int p, int s) { return PageResult.empty(); }
    @Override public Dict getById(Long id) { return null; }
    @Override public List<DictData> getDataByDictCode(String c) { return List.of(); }
    @Override public List<DictData> getDataByDictId(Long id) { return List.of(); }
    @Override public void create(Dict dict) { }
    @Override public void update(Dict dict) { }
    @Override public void delete(Long id) { }
    @Override public void createData(DictData data) { }
    @Override public void updateData(DictData data) { }
    @Override public void deleteData(Long id) { }
}