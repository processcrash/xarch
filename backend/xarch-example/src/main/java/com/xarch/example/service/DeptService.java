package com.xarch.example.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.entity.Dept;
import com.xarch.example.mapper.DeptMapper;
import com.xarch.starter.core.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Department service
 */
@Service
public class DeptService {

    @Autowired
    private DeptMapper deptMapper;

    public PageResult<Dept> page(String deptName, int pageNum, int pageSize) {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_dept").where("del_flag = 0");
        if (StringUtils.hasText(deptName)) {
            wrapper.and("dept_name LIKE ?", "%" + deptName + "%");
        }
        wrapper.orderBy("sort_order", true);

        Page<Dept> page = deptMapper.paginate(pageNum, pageSize, wrapper);
        return PageResult.of(page.getRecords(), page.getTotalRow());
    }

    public Dept getById(Long id) {
        return deptMapper.selectById(id);
    }

    public List<Dept> list() {
        QueryWrapper wrapper = QueryWrapper.create().from("sys_dept")
                .where("del_flag = 0")
                .orderBy("sort_order", true);
        return deptMapper.selectListByQuery(wrapper);
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