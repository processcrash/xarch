package com.xarch.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Opportunity create / update payload.
 */
public record OpportunityDTO(
        @NotNull
        Long customerId,

        @NotBlank
        String name,

        @NotNull
        BigDecimal amount,

        String currency,

        @NotBlank
        String stage,

        Integer probability,

        Long expectedCloseDate,

        Long ownerId,

        String description
) {
}
