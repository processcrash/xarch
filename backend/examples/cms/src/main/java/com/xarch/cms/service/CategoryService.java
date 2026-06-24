package com.xarch.cms.service;

import com.xarch.cms.entity.Category;

import java.util.List;

/**
 * Category business interface.
 */
public interface CategoryService {

    /** Get a category by id. */
    Category getById(Long id);

    /** Create a new category. */
    void create(Category category);

    /** Update an existing category. */
    void update(Category category);

    /** Soft delete a category (and detach its children to the parent). */
    void delete(Long id);

    /** List all categories as a flat list. */
    List<Category> list();

    /** Build a tree from the flat list. */
    List<Category> tree();
}
