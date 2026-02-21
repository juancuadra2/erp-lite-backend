package com.jcuadrado.erplitebackend.domain.model.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    @Test
    @DisplayName("create should build a valid, non-revoked, non-expired refresh token")
    void create_shouldBuildValidToken() {
        UUID userId = UUID.randomUUID();
        String tokenValue = "some-token-value";

        RefreshToken token = RefreshToken.create(userId, tokenValue, 7);

        assertThat(token.getId()).isNotNull();
        assertThat(token.getUserId()).isEqualTo(userId);
        assertThat(token.getToken()).isEqualTo(tokenValue);
        assertThat(token.isRevoked()).isFalse();
        assertThat(token.getCreatedAt()).isNotNull();
        assertThat(token.getExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("revoke should set revoked to true")
    void revoke_shouldSetRevokedTrue() {
        RefreshToken token = RefreshToken.create(UUID.randomUUID(), "token", 7);

        token.revoke();

        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    @DisplayName("isExpired should return false when token has not yet expired")
    void isExpired_shouldReturnFalse_whenTokenNotExpired() {
        RefreshToken token = RefreshToken.create(UUID.randomUUID(), "token", 7);

        assertThat(token.isExpired()).isFalse();
    }

    @Test
    @DisplayName("isExpired should return true when token is already expired")
    void isExpired_shouldReturnTrue_whenTokenExpired() {
        RefreshToken token = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .token("token")
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .revoked(false)
                .createdAt(LocalDateTime.now().minusDays(8))
                .build();

        assertThat(token.isExpired()).isTrue();
    }

    @Test
    @DisplayName("isValid should return true when token is not revoked and not expired")
    void isValid_shouldReturnTrue_whenNotRevokedAndNotExpired() {
        RefreshToken token = RefreshToken.create(UUID.randomUUID(), "token", 7);

        assertThat(token.isValid()).isTrue();
    }

    @Test
    @DisplayName("isValid should return false when token is revoked")
    void isValid_shouldReturnFalse_whenRevoked() {
        RefreshToken token = RefreshToken.create(UUID.randomUUID(), "token", 7);
        token.revoke();

        assertThat(token.isValid()).isFalse();
    }

    @Test
    @DisplayName("isValid should return false when token is expired")
    void isValid_shouldReturnFalse_whenExpired() {
        RefreshToken token = RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .token("token")
                .expiresAt(LocalDateTime.now().minusSeconds(1))
                .revoked(false)
                .createdAt(LocalDateTime.now().minusDays(8))
                .build();

        assertThat(token.isValid()).isFalse();
    }
}
