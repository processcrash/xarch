package com.xarch.example.ai.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.xarch.example.ai.entity.Skill;
import com.xarch.example.ai.mapper.SkillMapper;
import com.xarch.example.ai.service.SkillService;
import com.xarch.starter.core.result.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Stub Skill service — persists skill definitions and returns a
 * deterministic stub result for execution. Production code should
 * dispatch the skill to the appropriate model / tool pipeline.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkillServiceImpl implements SkillService {

    private final SkillMapper skillMapper;

    @Override
    public PageResult<Skill> page(String keyword, String category, Integer status,
                                  int pageNum, int pageSize) {
        try {
            QueryWrapper wrapper = QueryWrapper.create().where("del_flag = 0");
            if (keyword != null && !keyword.isBlank()) {
                wrapper.and("name LIKE ? OR code LIKE ?",
                        "%" + keyword + "%", "%" + keyword + "%");
            }
            if (category != null && !category.isBlank()) {
                wrapper.and("category = ?", category);
            }
            if (status != null) {
                wrapper.and("status = ?", status);
            }
            wrapper.orderBy("create_time", false);
            var page = skillMapper.paginate(pageNum, pageSize, wrapper);
            return PageResult.of(page.getRecords(), page.getTotalRow());
        } catch (Exception e) {
            log.warn("SkillService.page unavailable: {}", e.getMessage());
            return PageResult.of(List.of(), 0L);
        }
    }

    @Override
    public List<Skill> listInstalled() {
        try {
            return skillMapper.selectListByQuery(
                    QueryWrapper.create()
                            .where("del_flag = 0 AND status = 1")
                            .orderBy("name", true));
        } catch (Exception e) {
            log.warn("SkillService.listInstalled unavailable: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public Skill getById(Long id) {
        try {
            return skillMapper.selectOneById(id);
        } catch (Exception e) {
            log.warn("SkillService.getById unavailable: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Skill getByCode(String code) {
        try {
            return skillMapper.selectOneByQuery(
                    QueryWrapper.create()
                            .where("del_flag = 0 AND code = ?", code));
        } catch (Exception e) {
            log.warn("SkillService.getByCode unavailable: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional
    public Skill create(Skill skill) {
        skill.setCreateTime(LocalDateTime.now());
        skill.setUpdateTime(LocalDateTime.now());
        if (skill.getDelFlag() == null) {
            skill.setDelFlag(0);
        }
        if (skill.getStatus() == null) {
            skill.setStatus(0);
        }
        skillMapper.insert(skill);
        return skill;
    }

    @Override
    @Transactional
    public Skill update(Skill skill) {
        skill.setUpdateTime(LocalDateTime.now());
        skillMapper.updateById(skill);
        return skill;
    }

    @Override
    @Transactional
    public boolean install(Long id) {
        try {
            Skill skill = skillMapper.selectOneById(id);
            if (skill == null) {
                return false;
            }
            skill.setStatus(1);
            skill.setUpdateTime(LocalDateTime.now());
            skillMapper.updateById(skill);
            return true;
        } catch (Exception e) {
            log.warn("SkillService.install failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public boolean uninstall(Long id) {
        try {
            Skill skill = skillMapper.selectOneById(id);
            if (skill == null) {
                return false;
            }
            skill.setStatus(2);
            skill.setDelFlag(1);
            skill.setUpdateTime(LocalDateTime.now());
            skillMapper.updateById(skill);
            return true;
        } catch (Exception e) {
            log.warn("SkillService.uninstall failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> execute(Long id, Map<String, Object> parameters) {
        // Production: dispatch the skill to the configured LLM / tool chain.
        // The current stub returns a deterministic echo of the parameters.
        return Map.of(
                "skillId", id,
                "status", "stub-ok",
                "parameters", parameters != null ? parameters : Map.of()
        );
    }
}
