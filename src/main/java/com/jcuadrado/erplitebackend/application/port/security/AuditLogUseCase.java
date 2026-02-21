package com.jcuadrado.erplitebackend.application.port.security;

import com.jcuadrado.erplitebackend.application.command.security.AuditLogFilter;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AuditLogUseCase {

    Page<AuditLog> getAuditLogs(AuditLogFilter filter, Pageable pageable);

    Optional<AuditLog> getById(UUID id);
}
