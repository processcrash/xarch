package com.xarch.example.ai.service;

import com.xarch.example.ai.entity.Skill;
import com.xarch.starter.core.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * AI skill service contract.
 */
public interface SkillService {

    /**
     * Page through skills.
     */
    PageResult<Skill> page(String keyword, String category, Integer status,
                           int pageNum, int pageSize);

    /**
     * List every installed skill.
     */
    List<Skill> listInstalled();

    /**
     * Get a skill by primary key.
     */
    Skill getById(Long id);

    /**
     * Get a skill by code.
     */
    Skill getByCode(String code);

    /**
     * Create a new skill.
     */
    Skill create(Skill skill);

    /**
     * Update an existing skill.
     */
    Skill update(Skill skill);

    /**
     * Install a skill (status -&gt; installed).
     */
    boolean install(Long id);

    /**
     * Uninstall a skill (logical delete).
     */
    boolean uninstall(Long id);

    /**
     * Execute a skill with the supplied parameters.
     */
    Map<String, Object> execute(Long id, Map<String, Object> parameters);
}
