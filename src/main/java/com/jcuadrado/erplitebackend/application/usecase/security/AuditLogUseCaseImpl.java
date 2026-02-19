package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.command.security.AuditLogFilter;
import com.jcuadrado.erplitebackend.application.port.security.AuditLogUseCase;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.domain.port.security.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class AuditLogUseCaseImpl implements AuditLogUseCase {

    private final AuditLogRepository auditLogRepository;

    @Override
    public Page<AuditLog> getAuditLogs(AuditLogFilter filter, Pageable pageable) {
        return auditLogRepository.findByFilter(filter, pageable);
    }

    @Override
    public Optional<AuditLog> getById(UUID id) {
        return auditLogRepository.findById(id);
    }
}
