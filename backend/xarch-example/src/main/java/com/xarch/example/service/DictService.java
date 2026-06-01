package com.xarch.example.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.entity.Dict;
import com.xarch.example.entity.DictData;
import com.xarch.example.mapper.DictMapper;
import com.xarch.example.mapper.DictDataMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
        QueryWrapper wrapper = QueryWrapper.create().from("sys_dict").where("del_flag = 0");
        if (StringUtils.hasText(dictName)) {
            wrapper.and("dict_name LIKE ?", "%" + dictName + "%");
        }
        wrapper.orderBy("create_time", false);

        Page<Dict> page = dictMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    public Dict getById(Long id) {
        return dictMapper.selectById(id);
    }

    public List<Dict> list() {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_dict").where("del_flag = 0");
        return dictMapper.selectListByQuery(wrapper);
    }

    public List<DictData> getDataByDictCode(String dictCode) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_dict")
                .where("dict_code = ?", dictCode)
                .limit(1);
        Dict dict = dictMapper.selectOneByQuery(wrapper);

        if (dict == null) return List.of();

        QueryWrapper dataWrapper = QueryWrapper.create().from("sys_dict_data")
                .where("dict_id = ?", dict.getId())
                .orderBy("sort_order", true);
        return dictDataMapper.selectListByQuery(dataWrapper);
    }

    public List<DictData> getDataByDictId(Long dictId) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_dict_data")
                .where("dict_id = ?", dictId)
                .orderBy("sort_order", true);
        return dictDataMapper.selectListByQuery(wrapper);
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