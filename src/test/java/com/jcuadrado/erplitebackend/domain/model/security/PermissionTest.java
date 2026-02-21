package com.jcuadrado.erplitebackend.domain.model.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionTest {

    @Test
    @DisplayName("create should initialize permission with all provided fields and a generated id")
    void create_shouldInitializeAllFields() {
        Permission permission = Permission.create(
                "User", PermissionAction.READ, "owner", "Can read own user data");

        assertThat(permission.getId()).isNotNull();
        assertThat(permission.getEntity()).isEqualTo("User");
        assertThat(permission.getAction()).isEqualTo(PermissionAction.READ);
        assertThat(permission.getCondition()).isEqualTo("owner");
        assertThat(permission.getDescription()).isEqualTo("Can read own user data");
    }

    @Test
    @DisplayName("create should generate a unique id each time")
    void create_shouldGenerateUniqueId() {
        Permission p1 = Permission.create("User", PermissionAction.READ, null, "desc");
        Permission p2 = Permission.create("Role", PermissionAction.CREATE, null, "desc");

        assertThat(p1.getId()).isNotEqualTo(p2.getId());
    }

    @Test
    @DisplayName("create should allow null condition and description")
    void create_shouldAllowNullConditionAndDescription() {
        Permission permission = Permission.create("Product", PermissionAction.DELETE, null, null);

        assertThat(permission.getId()).isNotNull();
        assertThat(permission.getCondition()).isNull();
        assertThat(permission.getDescription()).isNull();
    }

    @Test
    @DisplayName("hasCondition should return true when condition is a non-blank string")
    void hasCondition_shouldReturnTrueWhenConditionIsPresent() {
        Permission permission = Permission.create("User", PermissionAction.UPDATE, "owner", "desc");

        assertThat(permission.hasCondition()).isTrue();
    }

    @Test
    @DisplayName("hasCondition should return false when condition is null")
    void hasCondition_shouldReturnFalseWhenConditionIsNull() {
        Permission permission = Permission.create("User", PermissionAction.CREATE, null, "desc");

        assertThat(permission.hasCondition()).isFalse();
    }

    @Test
    @DisplayName("hasCondition should return false when condition is a blank string")
    void hasCondition_shouldReturnFalseWhenConditionIsBlank() {
        Permission permission = Permission.create("User", PermissionAction.DELETE, "   ", "desc");

        assertThat(permission.hasCondition()).isFalse();
    }

    @Test
    @DisplayName("hasCondition should return false when condition is an empty string")
    void hasCondition_shouldReturnFalseWhenConditionIsEmpty() {
        Permission permission = Permission.create("User", PermissionAction.APPROVE, "", "desc");

        assertThat(permission.hasCondition()).isFalse();
    }
}
