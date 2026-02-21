package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.security;

import com.jcuadrado.erplitebackend.application.command.security.AuditLogFilter;
import com.jcuadrado.erplitebackend.application.port.security.AuditLogUseCase;
import com.jcuadrado.erplitebackend.domain.model.security.AuditAction;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.common.PagedResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.AuditLogResponseDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.mapper.security.AuditLogDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    @Mock
    private AuditLogUseCase auditLogUseCase;

    @Mock
    private AuditLogDtoMapper mapper;

    private AuditLogController controller;

    @BeforeEach
    void setUp() {
        controller = new AuditLogController(auditLogUseCase, mapper);
    }

    @Test
    @DisplayName("list should return 200 with a paged response of audit logs")
    void list_shouldReturn200_withPagedAuditLogs() {
        UUID logId = UUID.randomUUID();
        AuditLog log = AuditLog.create(UUID.randomUUID(), "admin", "User", UUID.randomUUID(),
                AuditAction.USER_CREATED, "127.0.0.1", "Agent");
        AuditLogResponseDto dto = AuditLogResponseDto.builder().id(logId).action("USER_CREATED").build();
        Page<AuditLog> page = new PageImpl<>(List.of(log), PageRequest.of(0, 20), 1);

        when(auditLogUseCase.getAuditLogs(any(AuditLogFilter.class), any())).thenReturn(page);
        when(mapper.toResponseDto(log)).thenReturn(dto);

        ResponseEntity<PagedResponseDto<AuditLogResponseDto>> response =
                controller.list(null, null, null, null, null, 0, 20, "timestamp", "desc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).containsExactly(dto);
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);
        verify(auditLogUseCase).getAuditLogs(any(AuditLogFilter.class), any());
    }

    @Test
    @DisplayName("list should parse action parameter and pass it in the filter")
    void list_shouldParseActionParam_andIncludeInFilter() {
        Page<AuditLog> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        when(auditLogUseCase.getAuditLogs(any(AuditLogFilter.class), any())).thenReturn(emptyPage);

        ResponseEntity<PagedResponseDto<AuditLogResponseDto>> response =
                controller.list(null, null, "LOGIN", null, null, 0, 20, "timestamp", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(auditLogUseCase).getAuditLogs(any(AuditLogFilter.class), any());
    }

    @Test
    @DisplayName("list should use ascending sort direction when direction param is 'asc'")
    void list_shouldUseAscendingSort_whenDirectionIsAsc() {
        Page<AuditLog> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(auditLogUseCase.getAuditLogs(any(AuditLogFilter.class), any())).thenReturn(emptyPage);

        ResponseEntity<PagedResponseDto<AuditLogResponseDto>> response =
                controller.list(null, "User", null, LocalDateTime.now().minusDays(1), LocalDateTime.now(),
                        0, 10, "timestamp", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("getById should return 200 with the audit log when found")
    void getById_shouldReturn200_whenFound() {
        UUID logId = UUID.randomUUID();
        AuditLog log = AuditLog.create(UUID.randomUUID(), "admin", "Role", UUID.randomUUID(),
                AuditAction.ROLE_CREATED, null, null);
        AuditLogResponseDto dto = AuditLogResponseDto.builder().id(logId).action("ROLE_CREATED").build();

        when(auditLogUseCase.getById(logId)).thenReturn(Optional.of(log));
        when(mapper.toResponseDto(log)).thenReturn(dto);

        ResponseEntity<AuditLogResponseDto> response = controller.getById(logId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(auditLogUseCase).getById(logId);
    }

    @Test
    @DisplayName("getById should return 404 when audit log is not found")
    void getById_shouldReturn404_whenNotFound() {
        UUID logId = UUID.randomUUID();

        when(auditLogUseCase.getById(logId)).thenReturn(Optional.empty());

        ResponseEntity<AuditLogResponseDto> response = controller.getById(logId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }
}
