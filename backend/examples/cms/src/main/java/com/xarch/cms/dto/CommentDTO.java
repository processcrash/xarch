package com.xarch.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Comment create payload.
 */
public record CommentDTO(
        @NotNull
        Long articleId,

        Long parentId,

        @NotBlank
        String content
) {
}
