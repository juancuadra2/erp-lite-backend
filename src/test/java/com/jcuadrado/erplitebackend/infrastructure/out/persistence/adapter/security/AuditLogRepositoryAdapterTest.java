package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.security;

import com.jcuadrado.erplitebackend.application.command.security.AuditLogFilter;
import com.jcuadrado.erplitebackend.domain.model.security.AuditAction;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.AuditLogJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.AuditLogEntity;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.security.AuditLogEntityMapper;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogRepositoryAdapterTest {

    @Mock
    private AuditLogJpaRepository jpaRepository;

    @Mock
    private AuditLogEntityMapper mapper;

    private AuditLogRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AuditLogRepositoryAdapter(jpaRepository, mapper);
    }

    @Test
    @DisplayName("save should map to entity, delegate to JPA, and map back to domain")
    void save_shouldMapAndDelegate() {
        AuditLog log = AuditLog.create(UUID.randomUUID(), "admin", "User", UUID.randomUUID(),
                AuditAction.USER_CREATED, "127.0.0.1", "Agent");
        AuditLogEntity entity = new AuditLogEntity();
        AuditLogEntity savedEntity = new AuditLogEntity();
        AuditLog savedLog = AuditLog.create(UUID.randomUUID(), "admin", "User", UUID.randomUUID(),
                AuditAction.USER_CREATED, "127.0.0.1", "Agent");

        when(mapper.toEntity(log)).thenReturn(entity);
        when(jpaRepository.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(savedLog);

        AuditLog result = adapter.save(log);

        assertThat(result).isNotNull();
        verify(mapper).toEntity(log);
        verify(jpaRepository).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("findById should return audit log when found")
    void findById_shouldReturnAuditLog_whenFound() {
        UUID logId = UUID.randomUUID();
        AuditLogEntity entity = new AuditLogEntity();
        AuditLog log = AuditLog.create(UUID.randomUUID(), "admin", "User", UUID.randomUUID(),
                AuditAction.LOGIN, "127.0.0.1", "Agent");

        when(jpaRepository.findById(logId)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(log);

        Optional<AuditLog> result = adapter.findById(logId);

        assertThat(result).isPresent();
        verify(jpaRepository).findById(logId);
    }

    @Test
    @DisplayName("findById should return empty when audit log is not found")
    void findById_shouldReturnEmpty_whenNotFound() {
        UUID logId = UUID.randomUUID();

        when(jpaRepository.findById(logId)).thenReturn(Optional.empty());

        Optional<AuditLog> result = adapter.findById(logId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByFilter should build specification and return paged audit logs")
    void findByFilter_shouldBuildSpecAndReturnPage() {
        AuditLogFilter filter = new AuditLogFilter(null, "User", AuditAction.LOGIN, null, null);
        Pageable pageable = PageRequest.of(0, 20);
        AuditLogEntity entity = new AuditLogEntity();
        AuditLog log = AuditLog.create(UUID.randomUUID(), "admin", "User", UUID.randomUUID(),
                AuditAction.LOGIN, "127.0.0.1", "Agent");
        Page<AuditLogEntity> entityPage = new PageImpl<>(List.of(entity), pageable, 1);

        when(jpaRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(entityPage);
        when(mapper.toDomain(entity)).thenReturn(log);

        Page<AuditLog> result = adapter.findByFilter(filter, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(log);
        verify(jpaRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}
