package com.xarch.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Follow-up create payload.
 */
public record FollowUpDTO(
        @NotNull
        Long customerId,

        Long contactId,

        Long opportunityId,

        @NotBlank
        String type,

        @NotBlank
        String content,

        String result,

        Long nextFollowUpDate,

        List<Long> attachments,

        Long userId
) {
}
