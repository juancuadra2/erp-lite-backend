package com.jcuadrado.erplitebackend.domain.model.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    @DisplayName("create should initialize role with active=true, a generated id and createdAt")
    void create_shouldInitializeDefaults() {
        Role role = Role.create("ADMIN", "Administrator role");

        assertThat(role.getId()).isNotNull();
        assertThat(role.getName()).isEqualTo("ADMIN");
        assertThat(role.getDescription()).isEqualTo("Administrator role");
        assertThat(role.isActive()).isTrue();
        assertThat(role.getCreatedAt()).isNotNull();
        assertThat(role.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("create should generate a unique id each time")
    void create_shouldGenerateUniqueId() {
        Role role1 = Role.create("ADMIN", "Admin");
        Role role2 = Role.create("USER", "User");

        assertThat(role1.getId()).isNotEqualTo(role2.getId());
    }

    @Test
    @DisplayName("update should change name and description and set updatedAt")
    void update_shouldChangeNameAndDescriptionAndSetUpdatedAt() {
        Role role = Role.create("OLD_NAME", "Old description");

        assertThat(role.getUpdatedAt()).isNull();

        role.update("NEW_NAME", "New description");

        assertThat(role.getName()).isEqualTo("NEW_NAME");
        assertThat(role.getDescription()).isEqualTo("New description");
        assertThat(role.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("update should allow changing only the description while keeping the same name")
    void update_shouldAllowChangingOnlyDescription() {
        Role role = Role.create("ADMIN", "Old description");

        role.update("ADMIN", "Updated description");

        assertThat(role.getName()).isEqualTo("ADMIN");
        assertThat(role.getDescription()).isEqualTo("Updated description");
        assertThat(role.getUpdatedAt()).isNotNull();
    }
}
