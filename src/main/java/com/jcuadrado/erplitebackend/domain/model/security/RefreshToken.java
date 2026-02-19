package com.jcuadrado.erplitebackend.domain.model.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RefreshToken {

    private UUID id;
    private UUID userId;
    private String token;
    private LocalDateTime expiresAt;
    private boolean revoked;
    private LocalDateTime createdAt;

    public static RefreshToken create(UUID userId, String token, int daysValid) {
        return RefreshToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(daysValid))
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }
}
