package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.command.security.AuditLogFilter;
import com.jcuadrado.erplitebackend.domain.model.security.AuditAction;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.domain.port.security.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
class AuditLogUseCaseImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogUseCaseImpl useCase;

    @Test
    @DisplayName("getAuditLogs should return a paged result from the repository")
    void getAuditLogs_shouldReturnPagedResult() {
        AuditLogFilter filter = new AuditLogFilter(null, null, null, null, null);
        Pageable pageable = PageRequest.of(0, 20);
        AuditLog log = AuditLog.create(UUID.randomUUID(), "admin", "User", UUID.randomUUID(),
                AuditAction.USER_CREATED, "127.0.0.1", "Agent");
        Page<AuditLog> page = new PageImpl<>(List.of(log), pageable, 1);

        when(auditLogRepository.findByFilter(filter, pageable)).thenReturn(page);

        Page<AuditLog> result = useCase.getAuditLogs(filter, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).containsExactly(log);
        verify(auditLogRepository).findByFilter(filter, pageable);
    }

    @Test
    @DisplayName("getById should return the audit log when found")
    void getById_shouldReturnAuditLog_whenFound() {
        UUID logId = UUID.randomUUID();
        AuditLog log = AuditLog.create(UUID.randomUUID(), "admin", "Role", UUID.randomUUID(),
                AuditAction.ROLE_CREATED, null, null);

        when(auditLogRepository.findById(logId)).thenReturn(Optional.of(log));

        Optional<AuditLog> result = useCase.getById(logId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(log);
        verify(auditLogRepository).findById(logId);
    }

    @Test
    @DisplayName("getById should return empty when audit log is not found")
    void getById_shouldReturnEmpty_whenNotFound() {
        UUID logId = UUID.randomUUID();

        when(auditLogRepository.findById(logId)).thenReturn(Optional.empty());

        Optional<AuditLog> result = useCase.getById(logId);

        assertThat(result).isEmpty();
        verify(auditLogRepository).findById(logId);
    }
}
