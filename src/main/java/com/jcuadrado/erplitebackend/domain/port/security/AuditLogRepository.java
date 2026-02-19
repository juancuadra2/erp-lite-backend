package com.jcuadrado.erplitebackend.domain.port.security;

import com.jcuadrado.erplitebackend.application.command.security.AuditLogFilter;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);

    Optional<AuditLog> findById(UUID id);

    Page<AuditLog> findByFilter(AuditLogFilter filter, Pageable pageable);
}
