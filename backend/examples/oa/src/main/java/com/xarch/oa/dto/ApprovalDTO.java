package com.xarch.oa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Approval action payload. The same shape is used for approve, reject
 * and transfer; {@code action} discriminates.
 */
public record ApprovalDTO(
        @NotBlank
        String action,

        @NotNull
        Long approverId,

        String approverName,

        String comment,

        /** Required when {@code action == TRANSFER}. */
        Long transferTo
) {
}
