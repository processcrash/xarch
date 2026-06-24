package com.xarch.starter.core.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BaseUserInfo}.
 *
 * <p>Verifies getter/setter round-trips for each property of the base user
 * info DTO.</p>
 */
@DisplayName("BaseUserInfo Tests")
class BaseUserInfoTest {

    @Test
    @DisplayName("userId getter and setter round-trip")
    void userId_roundtrips() {
        BaseUserInfo info = new BaseUserInfo();
        info.setUserId(42L);

        assertThat(info.getUserId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("username getter and setter round-trip")
    void username_roundtrips() {
        BaseUserInfo info = new BaseUserInfo();
        info.setUsername("admin");

        assertThat(info.getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("nickname getter and setter round-trip")
    void nickname_roundtrips() {
        BaseUserInfo info = new BaseUserInfo();
        info.setNickname("Admin");

        assertThat(info.getNickname()).isEqualTo("Admin");
    }

    @Test
    @DisplayName("deptId getter and setter round-trip")
    void deptId_roundtrips() {
        BaseUserInfo info = new BaseUserInfo();
        info.setDeptId(7L);

        assertThat(info.getDeptId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("deptName getter and setter round-trip")
    void deptName_roundtrips() {
        BaseUserInfo info = new BaseUserInfo();
        info.setDeptName("Engineering");

        assertThat(info.getDeptName()).isEqualTo("Engineering");
    }

    @Test
    @DisplayName("Default values are null")
    void defaultValues_areNull() {
        BaseUserInfo info = new BaseUserInfo();
        assertThat(info.getUserId()).isNull();
        assertThat(info.getUsername()).isNull();
        assertThat(info.getNickname()).isNull();
        assertThat(info.getDeptId()).isNull();
        assertThat(info.getDeptName()).isNull();
    }
}