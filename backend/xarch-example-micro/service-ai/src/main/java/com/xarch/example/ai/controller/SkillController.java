package com.xarch.example.ai.controller;

import com.xarch.starter.core.result.ApiResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Skill Controller — manages user-defined AI skills.
 *
 * <p><b>Status: planned.</b>
 */
@Tag(name = "AI Skills", description = "User-defined AI skills (planned)")
@RestController
@RequestMapping("/ai/skills")
@RequiredArgsConstructor
public class SkillController {

    /**
     * List all skills.
     */
    @GetMapping
    public ApiResult<List<Map<String, Object>>> list() {
        return ApiResult.success(List.of());
    }

    /**
     * Create a new skill.
     */
    @PostMapping
    public ApiResult<Void> create(@RequestBody Map<String, Object> body) {
        return ApiResult.ok();
    }

    /**
     * Delete a skill by id.
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id) {
        return ApiResult.ok();
    }
}