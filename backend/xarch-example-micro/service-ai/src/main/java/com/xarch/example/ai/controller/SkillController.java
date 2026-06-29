package com.xarch.example.ai.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.xarch.example.ai.entity.Skill;
import com.xarch.example.ai.service.SkillService;
import com.xarch.starter.core.annotation.XarchLog;
import com.xarch.starter.core.result.ApiResult;
import com.xarch.starter.core.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI skill controller — manages user-defined AI skills.
 *
 * <p>Skills are reusable named procedures the agent can invoke with a
 * parameter map. Production code should dispatch {@code /execute} to
 * the configured LLM / tool chain.</p>
 */
@Slf4j
@Tag(name = "AI Skills", description = "User-defined AI skills")
@RestController
@RequestMapping("/ai/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    /**
     * Page through skills.
     */
    @GetMapping
    @Operation(summary = "Page through skills")
    public ApiResult<PageResult<Skill>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResult.success(skillService.page(keyword, category, status, pageNum, pageSize));
    }

    /**
     * List every installed skill.
     */
    @GetMapping("/installed")
    @Operation(summary = "List every installed skill")
    public ApiResult<List<Skill>> listInstalled() {
        return ApiResult.success(skillService.listInstalled());
    }

    /**
     * Get a skill by id.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get skill detail")
    public ApiResult<Skill> detail(@PathVariable Long id) {
        Skill skill = skillService.getById(id);
        if (skill == null) {
            return ApiResult.fail("Skill not found");
        }
        return ApiResult.success(skill);
    }

    /**
     * Get a skill by code.
     */
    @GetMapping("/by-code/{code}")
    @Operation(summary = "Get skill detail by code")
    public ApiResult<Skill> getByCode(@PathVariable String code) {
        Skill skill = skillService.getByCode(code);
        if (skill == null) {
            return ApiResult.fail("Skill not found");
        }
        return ApiResult.success(skill);
    }

    /**
     * Create a new skill.
     */
    @PostMapping
    @XarchLog(value = "Create skill", type = "CREATE")
    @Operation(summary = "Create a new skill")
    public ApiResult<Skill> create(@RequestBody Skill skill) {
        skill.setCreateUserId(StpUtil.getLoginIdAsLong());
        skill.setCreateUserName(StpUtil.getLoginIdAsString());
        return ApiResult.success(skillService.create(skill));
    }

    /**
     * Update an existing skill.
     */
    @PutMapping("/{id}")
    @XarchLog(value = "Update skill", type = "UPDATE")
    @Operation(summary = "Update a skill")
    public ApiResult<Skill> update(@PathVariable Long id, @RequestBody Skill skill) {
        skill.setId(id);
        return ApiResult.success(skillService.update(skill));
    }

    /**
     * Install a skill.
     */
    @PostMapping("/{id}/install")
    @XarchLog(value = "Install skill", type = "OPERATION")
    @Operation(summary = "Install a skill")
    public ApiResult<Void> install(@PathVariable Long id) {
        boolean ok = skillService.install(id);
        return ok ? ApiResult.success(null) : ApiResult.fail("Failed to install skill");
    }

    /**
     * Uninstall a skill.
     */
    @PostMapping("/{id}/uninstall")
    @XarchLog(value = "Uninstall skill", type = "DELETE")
    @Operation(summary = "Uninstall a skill")
    public ApiResult<Void> uninstall(@PathVariable Long id) {
        boolean ok = skillService.uninstall(id);
        return ok ? ApiResult.success(null) : ApiResult.fail("Failed to uninstall skill");
    }

    /**
     * Hard-delete a skill.
     */
    @DeleteMapping("/{id}")
    @XarchLog(value = "Delete skill", type = "DELETE")
    @Operation(summary = "Delete a skill")
    public ApiResult<Void> delete(@PathVariable Long id) {
        boolean ok = skillService.uninstall(id);
        return ok ? ApiResult.success(null) : ApiResult.fail("Failed to delete skill");
    }

    /**
     * Execute a skill.
     */
    @PostMapping("/{id}/execute")
    @XarchLog(value = "Execute skill", type = "OPERATION")
    @Operation(summary = "Execute a skill with the supplied parameters")
    public ApiResult<Map<String, Object>> execute(@PathVariable Long id,
                                                  @RequestBody(required = false) ExecuteRequest request) {
        Map<String, Object> parameters = request != null ? request.getParameters() : Map.of();
        return ApiResult.success(skillService.execute(id, parameters));
    }

    /** Execute request payload. */
    @Data
    public static class ExecuteRequest {
        private Map<String, Object> parameters;
    }
}
