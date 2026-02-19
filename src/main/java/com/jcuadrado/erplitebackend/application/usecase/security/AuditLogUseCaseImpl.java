package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.command.security.AuditLogFilter;
import com.jcuadrado.erplitebackend.application.port.security.AuditLogUseCase;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.domain.port.security.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class AuditLogUseCaseImpl implements AuditLogUseCase {

    private final AuditLogRepository auditLogRepository;

    @Override
    public Page<AuditLog> getAuditLogs(AuditLogFilter filter, Pageable pageable) {
        log.debug("Consultando audit logs con filtros");
        return auditLogRepository.findByFilter(filter, pageable);
    }

    @Override
    public Optional<AuditLog> getById(UUID id) {
        log.debug("Buscando audit log por id: {}", id);
        return auditLogRepository.findById(id);
    }
}
