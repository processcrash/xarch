package com.xarch.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Article create / update payload.
 */
public record ArticleDTO(
        Long id,

        @NotBlank
        @Size(max = 256)
        String title,

        @NotBlank
        String content,

        String summary,

        Long categoryId,

        List<Long> tagIds
) {
}
