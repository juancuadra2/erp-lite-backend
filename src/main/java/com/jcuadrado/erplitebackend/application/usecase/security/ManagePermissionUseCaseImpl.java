package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.command.security.CreatePermissionCommand;
import com.jcuadrado.erplitebackend.application.port.security.ConditionEvaluator;
import com.jcuadrado.erplitebackend.application.port.security.ManagePermissionUseCase;
import com.jcuadrado.erplitebackend.domain.model.security.AuditAction;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.domain.model.security.Permission;
import com.jcuadrado.erplitebackend.domain.model.security.PermissionAction;
import com.jcuadrado.erplitebackend.domain.port.security.AuditLogRepository;
import com.jcuadrado.erplitebackend.domain.port.security.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class ManagePermissionUseCaseImpl implements ManagePermissionUseCase {

    private final PermissionRepository permissionRepository;
    private final AuditLogRepository auditLogRepository;
    private final ConditionEvaluator conditionEvaluator;

    @Override
    public Permission createPermission(CreatePermissionCommand command) {
        log.info("Creando permiso: {}:{}", command.entity(), command.action());

        PermissionAction action = PermissionAction.valueOf(command.action());
        Permission permission = Permission.create(command.entity(), action, command.condition(), command.description());
        Permission saved = permissionRepository.save(permission);

        log.info("Permiso creado con id: {}", saved.getId());
        return saved;
    }

    @Override
    public List<Permission> listAll() {
        log.debug("Listando todos los permisos");
        return permissionRepository.findAll();
    }

    @Override
    public boolean checkPermission(UUID userId, String entity, String action, Map<String, Object> context) {
        log.debug("Verificando permiso {}:{} para usuario: {}", entity, action, userId);

        List<Permission> userPermissions = permissionRepository.findByUserId(userId);
        PermissionAction requestedAction = PermissionAction.valueOf(action);

        boolean hasPermission = userPermissions.stream()
                .filter(p -> p.getEntity().equals(entity) && p.getAction() == requestedAction)
                .anyMatch(p -> {
                    if (!p.hasCondition()) {
                        return true;
                    }
                    return conditionEvaluator.evaluate(p.getCondition(), context);
                });

        if (!hasPermission) {
            auditLogRepository.save(AuditLog.create(
                    userId, null, entity, null,
                    AuditAction.PERMISSION_DENIED, null, null));
        }

        return hasPermission;
    }
}
