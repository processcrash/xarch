package com.xarch.crm.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Customer create / update payload.
 */
public record CustomerDTO(
        @NotBlank
        String name,

        String type,

        String industry,

        String scale,

        String contactName,

        String contactPhone,

        String contactEmail,

        String address,

        String website,

        Long ownerId,

        String source,

        String level,

        List<String> tags
) {
}
