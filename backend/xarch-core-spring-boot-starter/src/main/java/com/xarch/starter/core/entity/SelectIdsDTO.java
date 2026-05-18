package com.xarch.starter.core.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Select IDs request
 */
@Data
@Schema(description = "Select IDs request")
public class SelectIdsDTO implements Serializable {

    @NotEmpty(message = "IDs cannot be empty")
    @Schema(description = "IDs to select")
    private List<Long> ids;
}