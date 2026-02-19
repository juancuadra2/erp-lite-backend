package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.security;

import com.jcuadrado.erplitebackend.domain.model.security.RefreshToken;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.RefreshTokenJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.RefreshTokenEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.security.RefreshTokenEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRepositoryAdapterTest {

    @Mock
    private RefreshTokenJpaRepository jpaRepository;

    @Mock
    private RefreshTokenEntityMapper mapper;

    private RefreshTokenRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RefreshTokenRepositoryAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("save should map to entity, delegate to JPA, and map back to domain")
    void save_shouldMapAndDelegate() {
        UUID userId = UUID.randomUUID();
        RefreshToken token = RefreshToken.create(userId, "some-token", 7);
        RefreshTokenEntity entity = new RefreshTokenEntity();
        RefreshTokenEntity savedEntity = new RefreshTokenEntity();
        RefreshToken savedToken = RefreshToken.create(userId, "some-token", 7);

        when(mapper.toEntity(token)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedToken);

        RefreshToken result = adapter.save(token);

        assertThat(result).isNotNull();
        verify(mapper).toEntity(token);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("findByToken should return token when found")
    void findByToken_shouldReturnToken_whenFound() {
        String tokenValue = "some-token-value";
        RefreshTokenEntity entity = new RefreshTokenEntity();
        RefreshToken token = RefreshToken.create(UUID.randomUUID(), tokenValue, 7);

        when(jpaRepository.findByToken(tokenValue)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(token);

        Optional<RefreshToken> result = adapter.findByToken(tokenValue);

        assertThat(result).isPresent();
        verify(jpaRepository).findByToken(tokenValue);
    }

    @Test
    @DisplayName("findByToken should return empty when token is not found")
    void findByToken_shouldReturnEmpty_whenNotFound() {
        when(jpaRepository.findByToken("unknown")).thenReturn(Optional.empty());

        Optional<RefreshToken> result = adapter.findByToken("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("revokeAllByUserId should delegate to JPA repository")
    void revokeAllByUserId_shouldDelegate() {
        UUID userId = UUID.randomUUID();

        doNothing().when(jpaRepository).revokeAllByUserId(userId);

        adapter.revokeAllByUserId(userId);

        verify(jpaRepository).revokeAllByUserId(userId);
    }
}
