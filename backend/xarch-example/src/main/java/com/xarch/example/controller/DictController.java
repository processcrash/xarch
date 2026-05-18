package com.xarch.example.controller;

import com.xarch.example.entity.Dict;
import com.xarch.example.entity.DictData;
import com.xarch.example.service.DictService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Dictionary controller
 */
@RestController
@RequestMapping("/api/dicts")
public class DictController {

    @Autowired
    private DictService dictService;

    @GetMapping
    @XarchLog(value = "Query dictionary list", type = "QUERY")
    public ApiResult<PageResult<Dict>> page(
            @RequestParam(required = false) String dictName,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResult.ok(dictService.page(dictName, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResult<Dict> detail(@PathVariable Long id) {
        return ApiResult.ok(dictService.getById(id));
    }

    @GetMapping("/data/{dictCode}")
    public ApiResult<List<DictData>> getDataByCode(@PathVariable String dictCode) {
        return ApiResult.ok(dictService.getDataByDictCode(dictCode));
    }

    @GetMapping("/data/id/{dictId}")
    public ApiResult<List<DictData>> getDataById(@PathVariable Long dictId) {
        return ApiResult.ok(dictService.getDataByDictId(dictId));
    }

    @PostMapping
    @XarchLog(value = "Create dictionary", type = "CREATE")
    public ApiResult<Void> create(@RequestBody Dict dict) {
        dictService.create(dict);
        return ApiResult.ok();
    }

    @PutMapping("/{id}")
    @XarchLog(value = "Update dictionary", type = "UPDATE")
    public ApiResult<Void> update(@PathVariable Long id, @RequestBody Dict dict) {
        dict.setId(id);
        dictService.update(dict);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete dictionary", type = "DELETE")
    public ApiResult<Void> delete(@PathVariable Long id) {
        dictService.delete(id);
        return ApiResult.ok();
    }

    @PostMapping("/data")
    @XarchLog(value = "Create dictionary data", type = "CREATE")
    public ApiResult<Void> createData(@RequestBody DictData dictData) {
        dictService.createData(dictData);
        return ApiResult.ok();
    }

    @PutMapping("/data/{id}")
    @XarchLog(value = "Update dictionary data", type = "UPDATE")
    public ApiResult<Void> updateData(@PathVariable Long id, @RequestBody DictData dictData) {
        dictData.setId(id);
        dictService.updateData(dictData);
        return ApiResult.ok();
    }

    @DeleteMapping("/data/{id}")
    @XarchLog(value = "Delete dictionary data", type = "DELETE")
    public ApiResult<Void> deleteData(@PathVariable Long id) {
        dictService.deleteData(id);
        return ApiResult.ok();
    }
}