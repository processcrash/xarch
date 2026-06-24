package com.xarch.starter.core.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LoginUser}.
 *
 * <p>Confirms inherited base-user fields, role/permission properties, and the
 * default userType value.</p>
 */
@DisplayName("LoginUser Tests")
class LoginUserTest {

    @Test
    @DisplayName("Inherited BaseUserInfo fields are accessible")
    void inheritsBaseUserInfoFields() {
        LoginUser user = new LoginUser();
        user.setUserId(1L);
        user.setUsername("admin");
        user.setNickname("Administrator");

        assertThat(user.getUserId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getNickname()).isEqualTo("Administrator");
    }

    @Test
    @DisplayName("roleIds getter and setter round-trip")
    void roleIds_roundtrips() {
        LoginUser user = new LoginUser();
        user.setRoleIds("1,2");

        assertThat(user.getRoleIds()).isEqualTo("1,2");
    }

    @Test
    @DisplayName("roleNames getter and setter round-trip")
    void roleNames_roundtrips() {
        LoginUser user = new LoginUser();
        user.setRoleNames("admin,user");

        assertThat(user.getRoleNames()).isEqualTo("admin,user");
    }

    @Test
    @DisplayName("permissions getter and setter round-trip")
    void permissions_roundtrips() {
        LoginUser user = new LoginUser();
        user.setPermissions("user:read");

        assertThat(user.getPermissions()).isEqualTo("user:read");
    }

    @Test
    @DisplayName("userType default is 2 (normal)")
    void defaultUserType_isNormal() {
        assertThat(new LoginUser().getUserType()).isEqualTo(2);
    }

    @Test
    @DisplayName("userType can be set to 1 (admin)")
    void userType_canBeSetToAdmin() {
        LoginUser user = new LoginUser();
        user.setUserType(1);

        assertThat(user.getUserType()).isEqualTo(1);
    }
}