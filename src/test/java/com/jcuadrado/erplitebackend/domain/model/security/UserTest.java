package com.jcuadrado.erplitebackend.domain.model.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("create should initialize user with active=true, failedAttempts=0, and a generated id")
    void create_shouldInitializeDefaults() {
        UUID createdBy = UUID.randomUUID();
        UUID documentTypeId = UUID.randomUUID();

        User user = User.create(
                "john_doe", "john@example.com", "hashedPassword",
                "John", "Doe", documentTypeId, "12345678", createdBy);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getUsername()).isEqualTo("john_doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPasswordHash()).isEqualTo("hashedPassword");
        assertThat(user.getFirstName()).isEqualTo("John");
        assertThat(user.getLastName()).isEqualTo("Doe");
        assertThat(user.getDocumentTypeId()).isEqualTo(documentTypeId);
        assertThat(user.getDocumentNumber()).isEqualTo("12345678");
        assertThat(user.isActive()).isTrue();
        assertThat(user.getFailedAttempts()).isEqualTo(0);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getCreatedBy()).isEqualTo(createdBy);
        assertThat(user.getDeletedAt()).isNull();
        assertThat(user.getLockedAt()).isNull();
    }

    @Test
    @DisplayName("create should generate a unique id each time")
    void create_shouldGenerateUniqueId() {
        User user1 = User.create("alice", "alice@example.com", "hash", "Alice", "A", null, null, null);
        User user2 = User.create("bob", "bob@example.com", "hash", "Bob", "B", null, null, null);

        assertThat(user1.getId()).isNotEqualTo(user2.getId());
    }

    @Test
    @DisplayName("incrementFailedAttempts should increase counter and set lastFailedLoginAt")
    void incrementFailedAttempts_shouldIncreaseCounter() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .active(true)
                .failedAttempts(0)
                .build();

        user.incrementFailedAttempts();

        assertThat(user.getFailedAttempts()).isEqualTo(1);
        assertThat(user.getLastFailedLoginAt()).isNotNull();
        assertThat(user.isActive()).isTrue();
        assertThat(user.getLockedAt()).isNull();
    }

    @Test
    @DisplayName("incrementFailedAttempts should lock account on the 5th failed attempt")
    void incrementFailedAttempts_shouldLockAccountOnFifthAttempt() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .active(true)
                .failedAttempts(4)
                .build();

        user.incrementFailedAttempts();

        assertThat(user.getFailedAttempts()).isEqualTo(5);
        assertThat(user.isActive()).isFalse();
        assertThat(user.getLockedAt()).isNotNull();
        assertThat(user.isLocked()).isTrue();
    }

    @Test
    @DisplayName("incrementFailedAttempts should not lock account before the 5th failed attempt")
    void incrementFailedAttempts_shouldNotLockBeforeFifthAttempt() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .active(true)
                .failedAttempts(3)
                .build();

        user.incrementFailedAttempts();

        assertThat(user.getFailedAttempts()).isEqualTo(4);
        assertThat(user.isActive()).isTrue();
        assertThat(user.getLockedAt()).isNull();
        assertThat(user.isLocked()).isFalse();
    }

    @Test
    @DisplayName("resetFailedAttempts should set counter to 0 and clear lastFailedLoginAt")
    void resetFailedAttempts_shouldResetCounterAndClearDate() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .active(true)
                .failedAttempts(3)
                .lastFailedLoginAt(LocalDateTime.now())
                .build();

        user.resetFailedAttempts();

        assertThat(user.getFailedAttempts()).isEqualTo(0);
        assertThat(user.getLastFailedLoginAt()).isNull();
    }

    @Test
    @DisplayName("recordSuccessfulLogin should reset failed attempts and set lastLogin")
    void recordSuccessfulLogin_shouldResetAttemptsAndSetLastLogin() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .active(true)
                .failedAttempts(2)
                .lastFailedLoginAt(LocalDateTime.now().minusMinutes(10))
                .build();

        user.recordSuccessfulLogin();

        assertThat(user.getFailedAttempts()).isEqualTo(0);
        assertThat(user.getLastFailedLoginAt()).isNull();
        assertThat(user.getLastLogin()).isNotNull();
    }

    @Test
    @DisplayName("unlock should set active=true, failedAttempts=0 and clear lockedAt")
    void unlock_shouldActivateUserAndClearLock() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .active(false)
                .failedAttempts(5)
                .lockedAt(LocalDateTime.now().minusHours(1))
                .build();

        user.unlock();

        assertThat(user.isActive()).isTrue();
        assertThat(user.getFailedAttempts()).isEqualTo(0);
        assertThat(user.getLockedAt()).isNull();
    }

    @Test
    @DisplayName("softDelete should set deletedAt to the current time")
    void softDelete_shouldSetDeletedAt() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .active(true)
                .failedAttempts(0)
                .build();

        assertThat(user.getDeletedAt()).isNull();

        user.softDelete();

        assertThat(user.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("update should change profile fields and set updatedAt")
    void update_shouldChangeProfileFieldsAndSetUpdatedAt() {
        UUID documentTypeId = UUID.randomUUID();
        UUID updatedBy = UUID.randomUUID();
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .email("old@example.com")
                .firstName("Old")
                .lastName("Name")
                .active(true)
                .failedAttempts(0)
                .build();

        user.update("new@example.com", "New", "Name", documentTypeId, "99999999", updatedBy);

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getFirstName()).isEqualTo("New");
        assertThat(user.getLastName()).isEqualTo("Name");
        assertThat(user.getDocumentTypeId()).isEqualTo(documentTypeId);
        assertThat(user.getDocumentNumber()).isEqualTo("99999999");
        assertThat(user.getUpdatedBy()).isEqualTo(updatedBy);
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("updatePasswordHash should change the hash and set updatedAt")
    void updatePasswordHash_shouldChangeHashAndSetUpdatedAt() {
        UUID updatedBy = UUID.randomUUID();
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .passwordHash("old-hash")
                .active(true)
                .failedAttempts(0)
                .build();

        user.updatePasswordHash("new-hash", updatedBy);

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(user.getUpdatedBy()).isEqualTo(updatedBy);
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("isLocked should return true when active=false and failedAttempts>=5")
    void isLocked_shouldReturnTrueWhenAccountIsLocked() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .active(false)
                .failedAttempts(5)
                .build();

        assertThat(user.isLocked()).isTrue();
    }

    @Test
    @DisplayName("isLocked should return false when user is active")
    void isLocked_shouldReturnFalseWhenUserIsActive() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .active(true)
                .failedAttempts(0)
                .build();

        assertThat(user.isLocked()).isFalse();
    }

    @Test
    @DisplayName("isLocked should return false when inactive but failedAttempts is less than 5")
    void isLocked_shouldReturnFalseWhenInactiveWithLowFailedAttempts() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .active(false)
                .failedAttempts(3)
                .build();

        assertThat(user.isLocked()).isFalse();
    }

    @Test
    @DisplayName("isDeleted should return true when deletedAt is set")
    void isDeleted_shouldReturnTrueWhenDeletedAtIsSet() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .active(true)
                .failedAttempts(0)
                .deletedAt(LocalDateTime.now())
                .build();

        assertThat(user.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("isDeleted should return false when deletedAt is null")
    void isDeleted_shouldReturnFalseWhenDeletedAtIsNull() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .active(true)
                .failedAttempts(0)
                .build();

        assertThat(user.isDeleted()).isFalse();
    }
}
