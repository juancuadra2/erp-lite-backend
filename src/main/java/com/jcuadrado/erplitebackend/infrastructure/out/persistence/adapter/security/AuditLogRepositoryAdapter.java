package com.jcuadrado.erplitebackend.infrastructure.out.persistence.adapter.security;

import com.jcuadrado.erplitebackend.application.command.security.AuditLogFilter;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.domain.port.security.AuditLogRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.AuditLogJpaRepository;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.mapper.security.AuditLogEntityMapper;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.util.security.AuditLogSpecificationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.jcuadrado.erplitebackend.infrastructure.out.persistence.entity.security.AuditLogEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepository {

    private final AuditLogJpaRepository jpaRepository;
    private final AuditLogEntityMapper mapper;

    @Override
    public AuditLog save(AuditLog auditLog) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(auditLog)));
    }

    @Override
    public Optional<AuditLog> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Page<AuditLog> findByFilter(AuditLogFilter filter, Pageable pageable) {
        Specification<AuditLogEntity> spec = AuditLogSpecificationUtil.buildSpecification(filter);
        return jpaRepository.findAll(spec, pageable).map(mapper::toDomain);
    }
}
