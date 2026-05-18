package com.xarch.example.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xarch.example.entity.Dept;
import com.xarch.example.mapper.DeptMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Department service
 */
@Service
public class DeptService {

    @Autowired
    private DeptMapper deptMapper;

    public PageResult<Dept> page(String deptName, int pageNum, int pageSize) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Dept>();
        if (deptName != null && !deptName.isEmpty()) {
            wrapper.like(Dept::getDeptName, deptName);
        }
        wrapper.orderByAsc(Dept::getSortOrder);

        Page<Dept> page = new Page<>(pageNum, pageSize);
        Page<Dept> result = deptMapper.selectPage(page, wrapper);

        return PageResult.of(result.getRecords(), result.getTotal());
    }

    public Dept getById(Long id) {
        return deptMapper.selectById(id);
    }

    public List<Dept> list() {
        return deptMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Dept>()
                .orderByAsc(Dept::getSortOrder)
        );
    }

    public List<Dept> tree() {
        List<Dept> allDepts = list();
        return buildTree(0L, allDepts);
    }

    private List<Dept> buildTree(Long parentId, List<Dept> allDepts) {
        return allDepts.stream()
            .filter(d -> d.getParentId().equals(parentId))
            .peek(d -> d.setChildren(buildTree(d.getId(), allDepts)))
            .toList();
    }

    public void create(Dept dept) {
        deptMapper.insert(dept);
    }

    public void update(Dept dept) {
        deptMapper.updateById(dept);
    }

    public void delete(Long id) {
        deptMapper.deleteById(id);
    }
}