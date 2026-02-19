package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.security;

import com.jcuadrado.erplitebackend.domain.model.security.RefreshToken;
import com.jcuadrado.erplitebackend.domain.port.security.RefreshTokenRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.RefreshTokenJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.security.RefreshTokenEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository jpaRepository;
    private final RefreshTokenEntityMapper mapper;

    @Override
    public RefreshToken save(RefreshToken token) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(token)));
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public void revokeAllByUserId(UUID userId) {
        jpaRepository.revokeAllByUserId(userId);
    }
}
