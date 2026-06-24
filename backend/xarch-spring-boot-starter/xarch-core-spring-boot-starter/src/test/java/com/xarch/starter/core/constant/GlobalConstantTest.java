package com.xarch.starter.core.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GlobalConstant}.
 *
 * <p>Ensures the public string constants match the expected values used across
 * the framework.</p>
 */
@DisplayName("GlobalConstant Tests")
class GlobalConstantTest {

    @Test
    @DisplayName("SUPER_ROLE is 'SUPER_ADMIN'")
    void superRole_hasExpectedValue() {
        assertThat(GlobalConstant.SUPER_ROLE).isEqualTo("SUPER_ADMIN");
    }

    @Test
    @DisplayName("CODE_PREFIX is 'xarch'")
    void codePrefix_hasExpectedValue() {
        assertThat(GlobalConstant.CODE_PREFIX).isEqualTo("xarch");
    }

    @Test
    @DisplayName("TOKEN_HEADER is 'Authorization'")
    void tokenHeader_hasExpectedValue() {
        assertThat(GlobalConstant.TOKEN_HEADER).isEqualTo("Authorization");
    }

    @Test
    @DisplayName("TOKEN_PREFIX is 'Bearer '")
    void tokenPrefix_hasExpectedValue() {
        assertThat(GlobalConstant.TOKEN_PREFIX).isEqualTo("Bearer ");
    }
}