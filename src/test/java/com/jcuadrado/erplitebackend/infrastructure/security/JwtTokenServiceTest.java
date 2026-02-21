package com.jcuadrado.erplitebackend.infrastructure.security;

import com.jcuadrado.erplitebackend.domain.model.security.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private JwtTokenService service;

    private static final String TEST_SECRET = "test-secret-key-for-unit-tests-32c!";
    private static final long EXPIRATION_SECONDS = 1800L;

    @BeforeEach
    void setUp() throws Exception {
        service = new JwtTokenService();

        Field secretField = JwtTokenService.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(service, TEST_SECRET);

        Field expirationField = JwtTokenService.class.getDeclaredField("accessTokenExpirationSeconds");
        expirationField.setAccessible(true);
        expirationField.set(service, EXPIRATION_SECONDS);
    }

    @Test
    @DisplayName("generateAccessToken should return a non-blank JWT string")
    void generateAccessToken_shouldReturnNonBlankToken() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .active(true)
                .failedAttempts(0)
                .build();

        String token = service.generateAccessToken(user, List.of("ADMIN"), List.of("Invoice:READ"));

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("extractUsername should return the subject from a valid token")
    void extractUsername_shouldReturnSubject_fromValidToken() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("alice")
                .active(true)
                .failedAttempts(0)
                .build();

        String token = service.generateAccessToken(user, List.of("USER"), List.of());
        String username = service.extractUsername(token);

        assertThat(username).isEqualTo("alice");
    }

    @Test
    @DisplayName("validateToken should return true for a valid token")
    void validateToken_shouldReturnTrue_forValidToken() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("bob")
                .active(true)
                .failedAttempts(0)
                .build();

        String token = service.generateAccessToken(user, List.of(), List.of());

        assertThat(service.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken should return false for a malformed or tampered token")
    void validateToken_shouldReturnFalse_forInvalidToken() {
        assertThat(service.validateToken("not.a.valid.jwt")).isFalse();
    }

    @Test
    @DisplayName("validateToken should return false for a blank token")
    void validateToken_shouldReturnFalse_forBlankToken() {
        assertThat(service.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("extractRoles should return the roles embedded in the token")
    void extractRoles_shouldReturnRoles_fromValidToken() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .active(true)
                .failedAttempts(0)
                .build();

        String token = service.generateAccessToken(user, List.of("ADMIN", "USER"), List.of());

        assertThat(service.extractRoles(token)).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    @DisplayName("extractRoles should return empty list when token has no roles")
    void extractRoles_shouldReturnEmptyList_whenNoRoles() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("guest")
                .active(true)
                .failedAttempts(0)
                .build();

        String token = service.generateAccessToken(user, List.of(), List.of());

        assertThat(service.extractRoles(token)).isEmpty();
    }

    @Test
    @DisplayName("extractPermissions should return the permissions embedded in the token")
    void extractPermissions_shouldReturnPermissions_fromValidToken() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .active(true)
                .failedAttempts(0)
                .build();

        String token = service.generateAccessToken(user, List.of(), List.of("WAREHOUSE:CREATE", "WAREHOUSE:READ"));

        assertThat(service.extractPermissions(token)).containsExactlyInAnyOrder("WAREHOUSE:CREATE", "WAREHOUSE:READ");
    }

    @Test
    @DisplayName("extractPermissions should return empty list when token has no permissions")
    void extractPermissions_shouldReturnEmptyList_whenNoPermissions() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("guest")
                .active(true)
                .failedAttempts(0)
                .build();

        String token = service.generateAccessToken(user, List.of(), List.of());

        assertThat(service.extractPermissions(token)).isEmpty();
    }

    @Test
    @DisplayName("extractRoles should return empty list when claim value is not a List")
    void extractRoles_shouldReturnEmptyList_whenClaimIsNotAList() {
        var key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("user")
                .claim("roles", "NOT_A_LIST")
                .claim("permissions", "NOT_A_LIST")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_SECONDS * 1000))
                .issuer("erp-lite")
                .signWith(key)
                .compact();

        assertThat(service.extractRoles(token)).isEmpty();
        assertThat(service.extractPermissions(token)).isEmpty();
    }
}
