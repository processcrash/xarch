package com.xarch.starter.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.tags.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link XarchSwaggerConfig}.
 *
 * <p>Confirms the OpenAPI bean is created with the expected title, version,
 * contact, license and tag list used by the framework.</p>
 */
@DisplayName("XarchSwaggerConfig Tests")
class XarchSwaggerConfigTest {

    private XarchSwaggerConfig config;

    @BeforeEach
    void setUp() {
        config = new XarchSwaggerConfig();
    }

    @Test
    @DisplayName("openAPI bean produces a non-null OpenAPI")
    void openAPI_returnsNonNull() {
        OpenAPI openAPI = config.openAPI();

        assertThat(openAPI).isNotNull();
    }

    @Test
    @DisplayName("OpenAPI info contains expected title and version")
    void openAPI_infoHasExpectedTitleAndVersion() {
        OpenAPI openAPI = config.openAPI();
        Info info = openAPI.getInfo();

        assertThat(info).isNotNull();
        assertThat(info.getTitle()).isEqualTo("xarch API Documentation");
        assertThat(info.getVersion()).isEqualTo("1.0.0");
        assertThat(info.getDescription()).contains("AI-Enabled Enterprise Backend Framework");
    }

    @Test
    @DisplayName("OpenAPI contact and license are populated")
    void openAPI_contactAndLicenseArePopulated() {
        OpenAPI openAPI = config.openAPI();

        assertThat(openAPI.getInfo().getContact()).isNotNull();
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("xarch");

        assertThat(openAPI.getInfo().getLicense()).isNotNull();
        assertThat(openAPI.getInfo().getLicense().getName()).isEqualTo("MIT");
    }

    @Test
    @DisplayName("OpenAPI tags are present and contain framework-defined tags")
    void openAPI_tagsContainExpectedEntries() {
        OpenAPI openAPI = config.openAPI();

        assertThat(openAPI.getTags()).isNotEmpty();
        assertThat(openAPI.getTags())
            .extracting(Tag::getName)
            .contains("System - User", "Common", "MCP - Database");
    }
}