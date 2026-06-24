package com.xarch.cms.service.impl;

import com.xarch.cms.entity.Tag;
import com.xarch.cms.mapper.TagMapper;
import com.xarch.cms.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Tag service implementation.
 */
@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    public Tag getById(Long id) {
        return tagMapper.selectOneById(id);
    }

    @Override
    public void create(Tag tag) {
        tagMapper.insert(tag);
    }

    @Override
    public void update(Tag tag) {
        tagMapper.updateById(tag);
    }

    @Override
    public void delete(Long id) {
        tagMapper.deleteById(id);
    }

    @Override
    public List<Tag> list() {
        return tagMapper.selectList();
    }
}
