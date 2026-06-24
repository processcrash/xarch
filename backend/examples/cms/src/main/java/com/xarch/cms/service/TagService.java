package com.xarch.cms.service;

import com.xarch.cms.entity.Tag;

import java.util.List;

/**
 * Tag business interface.
 */
public interface TagService {

    Tag getById(Long id);

    void create(Tag tag);

    void update(Tag tag);

    void delete(Long id);

    List<Tag> list();
}
