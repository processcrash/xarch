package com.xarch.example.system.service;

import com.xarch.example.system.entity.Dict;
import com.xarch.example.system.entity.DictData;
import com.xarch.starter.core.result.PageResult;

import java.util.List;

/** Dict service contract. */
public interface DictService {
    PageResult<Dict> page(String dictName, int pageNum, int pageSize);
    Dict getById(Long id);
    List<DictData> getDataByDictCode(String dictCode);
    List<DictData> getDataByDictId(Long dictId);
    void create(Dict dict);
    void update(Dict dict);
    void delete(Long id);
    void createData(DictData data);
    void updateData(DictData data);
    void deleteData(Long id);
}