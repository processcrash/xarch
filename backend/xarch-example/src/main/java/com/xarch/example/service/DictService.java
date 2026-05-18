package com.xarch.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.Dict;
import com.xarch.example.entity.DictData;
import com.xarch.example.mapper.DictMapper;
import com.xarch.example.mapper.DictDataMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Dictionary service
 */
@Service
public class DictService {

    @Autowired
    private DictMapper dictMapper;

    @Autowired
    private DictDataMapper dictDataMapper;

    public PageResult<Dict> page(String dictName, int pageNum, int pageSize) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Dict>();
        if (dictName != null && !dictName.isEmpty()) {
            wrapper.like(Dict::getDictName, dictName);
        }
        wrapper.orderByDesc(Dict::getCreateTime);

        Page<Dict> page = new Page<>(pageNum, pageSize);
        Page<Dict> result = dictMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    public Dict getById(Long id) {
        return dictMapper.selectById(id);
    }

    public List<Dict> list() {
        return dictMapper.selectList(null);
    }

    public List<DictData> getDataByDictCode(String dictCode) {
        Dict dict = dictMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Dict>()
                .eq(Dict::getDictCode, dictCode)
        ).stream().findFirst().orElse(null);

        if (dict == null) return List.of();

        return dictDataMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DictData>()
                .eq(DictData::getDictId, dict.getId())
                .orderByAsc(DictData::getSortOrder)
        );
    }

    public List<DictData> getDataByDictId(Long dictId) {
        return dictDataMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DictData>()
                .eq(DictData::getDictId, dictId)
                .orderByAsc(DictData::getSortOrder)
        );
    }

    public void create(Dict dict) {
        dictMapper.insert(dict);
    }

    public void update(Dict dict) {
        dictMapper.updateById(dict);
    }

    public void delete(Long id) {
        dictMapper.deleteById(id);
    }

    public void createData(DictData dictData) {
        dictDataMapper.insert(dictData);
    }

    public void updateData(DictData dictData) {
        dictDataMapper.updateById(dictData);
    }

    public void deleteData(Long id) {
        dictDataMapper.deleteById(id);
    }
}