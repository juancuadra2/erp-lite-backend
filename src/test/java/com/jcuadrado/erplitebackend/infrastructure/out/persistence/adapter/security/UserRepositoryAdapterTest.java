package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.security;

import com.jcuadrado.erplitebackend.domain.model.security.User;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.UserJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.UserEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.security.UserEntityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

    @Mock
    private UserJpaRepository jpaRepository;

    @Mock
    private UserEntityMapper mapper;

    private UserRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserRepositoryAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("save should map to entity, delegate to JPA, and map back to domain")
    void save_shouldMapAndDelegate() {
        User user = User.builder().id(UUID.randomUUID()).username("alice").active(true).failedAttempts(0).build();
        UserEntity entity = new UserEntity();
        UserEntity savedEntity = new UserEntity();
        User savedUser = User.builder().id(user.getId()).username("alice").active(true).failedAttempts(0).build();

        when(mapper.toEntity(user)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedUser);

        User result = adapter.save(user);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("alice");
        verify(mapper).toEntity(user);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("findById should return user when active user is found")
    void findById_shouldReturnUser_whenFound() {
        UUID userId = UUID.randomUUID();
        UserEntity entity = new UserEntity();
        User user = User.builder().id(userId).username("alice").active(true).failedAttempts(0).build();

        when(jpaRepository.findActiveById(userId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(user);

        Optional<User> result = adapter.findById(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("findById should return empty when user is not found")
    void findById_shouldReturnEmpty_whenNotFound() {
        UUID userId = UUID.randomUUID();

        when(jpaRepository.findActiveById(userId)).thenReturn(Optional.empty());

        Optional<User> result = adapter.findById(userId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUsername should return user when found")
    void findByUsername_shouldReturnUser_whenFound() {
        UserEntity entity = new UserEntity();
        User user = User.builder().id(UUID.randomUUID()).username("alice").active(true).failedAttempts(0).build();

        when(jpaRepository.findByUsername("alice")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(user);

        Optional<User> result = adapter.findByUsername("alice");

        assertThat(result).isPresent();
        verify(jpaRepository).findByUsername("alice");
    }

    @Test
    @DisplayName("findByEmail should return user when found")
    void findByEmail_shouldReturnUser_whenFound() {
        UserEntity entity = new UserEntity();
        User user = User.builder().id(UUID.randomUUID()).username("alice").active(true).failedAttempts(0).build();

        when(jpaRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(user);

        Optional<User> result = adapter.findByEmail("alice@example.com");

        assertThat(result).isPresent();
        verify(jpaRepository).findByEmail("alice@example.com");
    }

    @Test
    @DisplayName("findAll should return paged users")
    void findAll_shouldReturnPagedUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        UserEntity entity = new UserEntity();
        User user = User.builder().id(UUID.randomUUID()).username("alice").active(true).failedAttempts(0).build();
        Page<UserEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

        when(jpaRepository.findAllActive(pageable)).thenReturn(entityPage);
        when(mapper.toDomain(entity)).thenReturn(user);

        Page<User> result = adapter.findAll(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(user);
    }

    @Test
    @DisplayName("existsByUsername should delegate to JPA repository")
    void existsByUsername_shouldDelegate() {
        when(jpaRepository.existsByUsername("alice")).thenReturn(true);

        assertThat(adapter.existsByUsername("alice")).isTrue();
        verify(jpaRepository).existsByUsername("alice");
    }

    @Test
    @DisplayName("existsByEmail should delegate to JPA repository")
    void existsByEmail_shouldDelegate() {
        when(jpaRepository.existsByEmail("alice@example.com")).thenReturn(false);

        assertThat(adapter.existsByEmail("alice@example.com")).isFalse();
        verify(jpaRepository).existsByEmail("alice@example.com");
    }
}
