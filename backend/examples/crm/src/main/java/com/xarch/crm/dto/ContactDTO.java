package com.xarch.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Contact create / update payload.
 */
public record ContactDTO(
        /** Null on create, populated on update. */
        Long id,

        @NotNull
        Long customerId,

        @NotBlank
        String name,

        String position,

        String phone,

        String email,

        Boolean isPrimary
) {
}