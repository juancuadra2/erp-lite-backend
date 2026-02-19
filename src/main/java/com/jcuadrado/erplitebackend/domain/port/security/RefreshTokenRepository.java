package com.jcuadrado.erplitebackend.domain.port.security;

import com.jcuadrado.erplitebackend.domain.model.security.RefreshToken;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    void revokeAllByUserId(UUID userId);
}
