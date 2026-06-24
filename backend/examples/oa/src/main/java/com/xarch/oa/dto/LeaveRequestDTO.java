package com.xarch.oa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Create / update payload for a leave request.
 */
public record LeaveRequestDTO(
        @NotBlank
        String type,

        @NotNull
        Long startDate,

        @NotNull
        Long endDate,

        @NotBlank
        String reason,

        List<Long> attachments
) {
}
