package com.xarch.oa.dto;

import com.xarch.oa.entity.ExpenseItem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * Create / update payload for an expense report.
 */
public record ExpenseReportDTO(
        @NotBlank
        String category,

        @NotNull
        BigDecimal amount,

        @NotBlank
        String currency,

        String description,

        @NotEmpty
        List<ExpenseItem> items
) {
}
