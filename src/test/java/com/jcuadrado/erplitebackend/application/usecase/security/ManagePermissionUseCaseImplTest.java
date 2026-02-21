package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.command.security.CreatePermissionCommand;
import com.jcuadrado.erplitebackend.application.port.security.ConditionEvaluator;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.domain.model.security.Permission;
import com.jcuadrado.erplitebackend.domain.model.security.PermissionAction;
import com.jcuadrado.erplitebackend.domain.port.security.AuditLogRepository;
import com.jcuadrado.erplitebackend.domain.port.security.PermissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManagePermissionUseCaseImplTest {

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private ConditionEvaluator conditionEvaluator;

    private ManagePermissionUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ManagePermissionUseCaseImpl(permissionRepository, auditLogRepository, conditionEvaluator);
    }

    @Test
    @DisplayName("createPermission should create and save the permission")
    void createPermission_shouldCreateAndSave() {
        CreatePermissionCommand command = new CreatePermissionCommand("Invoice", "READ", null, "Read invoices");
        Permission saved = Permission.create("Invoice", PermissionAction.READ, null, "Read invoices");

        when(permissionRepository.save(any(Permission.class))).thenReturn(saved);

        Permission result = useCase.createPermission(command);

        assertThat(result).isNotNull();
        assertThat(result.getEntity()).isEqualTo("Invoice");
        assertThat(result.getAction()).isEqualTo(PermissionAction.READ);
        verify(permissionRepository).save(any(Permission.class));
    }

    @Test
    @DisplayName("listAll should return all permissions from the repository")
    void listAll_shouldReturnAllPermissions() {
        Permission p1 = Permission.create("Invoice", PermissionAction.READ, null, "Read");
        Permission p2 = Permission.create("Invoice", PermissionAction.CREATE, null, "Create");

        when(permissionRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Permission> result = useCase.listAll();

        assertThat(result).hasSize(2);
        verify(permissionRepository).findAll();
    }

    @Test
    @DisplayName("checkPermission should return true when user has matching permission without condition")
    void checkPermission_shouldReturnTrue_whenUserHasPermissionWithoutCondition() {
        UUID userId = UUID.randomUUID();
        Permission permission = Permission.create("Invoice", PermissionAction.READ, null, "Read invoices");

        when(permissionRepository.findByUserId(userId)).thenReturn(List.of(permission));

        boolean result = useCase.checkPermission(userId, "Invoice", "READ", Map.of());

        assertThat(result).isTrue();
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("checkPermission should evaluate SpEL condition and return true when condition is met")
    void checkPermission_shouldReturnTrue_whenConditionIsMet() {
        UUID userId = UUID.randomUUID();
        Permission permission = Permission.create("Invoice", PermissionAction.READ, "#dept == 'SALES'", "Read sales invoices");
        Map<String, Object> context = Map.of("dept", "SALES");

        when(permissionRepository.findByUserId(userId)).thenReturn(List.of(permission));
        when(conditionEvaluator.evaluate("#dept == 'SALES'", context)).thenReturn(true);

        boolean result = useCase.checkPermission(userId, "Invoice", "READ", context);

        assertThat(result).isTrue();
        verify(conditionEvaluator).evaluate("#dept == 'SALES'", context);
        verify(auditLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("checkPermission should return false and save audit log when user has no matching permission")
    void checkPermission_shouldReturnFalse_andSaveAuditLog_whenNoPermission() {
        UUID userId = UUID.randomUUID();

        when(permissionRepository.findByUserId(userId)).thenReturn(List.of());
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = useCase.checkPermission(userId, "Invoice", "DELETE", Map.of());

        assertThat(result).isFalse();
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("checkPermission should return false when SpEL condition is not met")
    void checkPermission_shouldReturnFalse_whenConditionNotMet() {
        UUID userId = UUID.randomUUID();
        Permission permission = Permission.create("Invoice", PermissionAction.READ, "#dept == 'SALES'", "Read sales invoices");
        Map<String, Object> context = Map.of("dept", "MARKETING");

        when(permissionRepository.findByUserId(userId)).thenReturn(List.of(permission));
        when(conditionEvaluator.evaluate("#dept == 'SALES'", context)).thenReturn(false);
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = useCase.checkPermission(userId, "Invoice", "READ", context);

        assertThat(result).isFalse();
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("checkPermission should return false when permissions exist but none match the requested entity and action")
    void checkPermission_shouldReturnFalse_whenPermissionsExistButNoneMatch() {
        UUID userId = UUID.randomUUID();
        Permission permission = Permission.create("Invoice", PermissionAction.READ, null, "Read invoices");
        when(permissionRepository.findByUserId(userId)).thenReturn(List.of(permission));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean result = useCase.checkPermission(userId, "Invoice", "DELETE", Map.of());

        assertThat(result).isFalse();
        verify(auditLogRepository).save(any(AuditLog.class));
    }
}
