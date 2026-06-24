package com.xarch.cms.service.impl;

import com.xarch.cms.entity.Category;
import com.xarch.cms.exception.CmsException;
import com.xarch.cms.mapper.CategoryMapper;
import com.xarch.cms.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Category service implementation. Builds a tree from the flat list of
 * categories using parent id and sort order.
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public Category getById(Long id) {
        return categoryMapper.selectOneById(id);
    }

    @Override
    public void create(Category category) {
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        categoryMapper.insert(category);
    }

    @Override
    public void update(Category category) {
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        // detach children to parent before removing
        Category self = categoryMapper.selectOneById(id);
        if (self == null) {
            throw new CmsException("Category not found: " + id);
        }
        for (Category candidate : categoryMapper.selectList()) {
            if (candidate.getParentId() != null && candidate.getParentId().equals(id)) {
                candidate.setParentId(self.getParentId());
                categoryMapper.updateById(candidate);
            }
        }
        categoryMapper.deleteById(id);
    }

    @Override
    public List<Category> list() {
        List<Category> all = categoryMapper.selectList();
        all.sort(Comparator.comparing(Category::getSortOrder,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return all;
    }

    @Override
    public List<Category> tree() {
        List<Category> all = list();
        Map<Long, Category> lookup = new HashMap<>(all.size());
        all.forEach(c -> {
            c.setChildren(new ArrayList<>());
            lookup.put(c.getId(), c);
        });
        List<Category> roots = new ArrayList<>();
        for (Category node : all) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0L || !lookup.containsKey(parentId)) {
                roots.add(node);
            } else {
                lookup.get(parentId).getChildren().add(node);
            }
        }
        return roots;
    }
}
