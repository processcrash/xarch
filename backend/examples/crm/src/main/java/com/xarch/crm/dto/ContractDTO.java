package com.xarch.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Contract create / update payload.
 */
public record ContractDTO(
        @NotNull
        Long customerId,

        Long opportunityId,

        @NotBlank
        String contractNo,

        @NotNull
        BigDecimal amount,

        Long startDate,

        Long endDate,

        String paymentTerms
) {
}
