package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.command.security.ChangePasswordCommand;
import com.jcuadrado.erplitebackend.application.command.security.CreateUserCommand;
import com.jcuadrado.erplitebackend.application.command.security.UpdateUserCommand;
import com.jcuadrado.erplitebackend.application.port.security.ManageUserUseCase;
import com.jcuadrado.erplitebackend.application.port.security.PasswordEncoder;
import com.jcuadrado.erplitebackend.domain.exception.security.DuplicateEmailException;
import com.jcuadrado.erplitebackend.domain.exception.security.DuplicateUsernameException;
import com.jcuadrado.erplitebackend.domain.exception.security.InvalidCredentialsException;
import com.jcuadrado.erplitebackend.domain.exception.security.RoleNotFoundException;
import com.jcuadrado.erplitebackend.domain.exception.security.UserNotFoundException;
import com.jcuadrado.erplitebackend.domain.model.security.AuditAction;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.domain.model.security.Role;
import com.jcuadrado.erplitebackend.domain.model.security.User;
import com.jcuadrado.erplitebackend.domain.port.security.AuditLogRepository;
import com.jcuadrado.erplitebackend.domain.port.security.RoleRepository;
import com.jcuadrado.erplitebackend.domain.port.security.UserRepository;
import com.jcuadrado.erplitebackend.domain.service.security.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class ManageUserUseCaseImpl implements ManageUserUseCase {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserDomainService userDomainService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(CreateUserCommand command) {
        log.info("Creando usuario: {}", command.username());

        userDomainService.validateUsername(command.username());
        userDomainService.validateEmail(command.email());
        userDomainService.validatePassword(command.password());

        if (userRepository.existsByUsername(command.username())) {
            throw new DuplicateUsernameException("Username ya existe: " + command.username());
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException("Email ya existe: " + command.email());
        }

        String passwordHash = passwordEncoder.encode(command.password());
        User user = User.create(
                command.username(), command.email(), passwordHash,
                command.firstName(), command.lastName(),
                command.documentTypeId(), command.documentNumber(),
                command.createdBy());

        User saved = userRepository.save(user);

        if (command.roleIds() != null && !command.roleIds().isEmpty()) {
            assignRoles(saved.getId(), command.roleIds());
        }

        auditLogRepository.save(AuditLog.create(
                command.createdBy(), null, "User", saved.getId(),
                AuditAction.USER_CREATED, null, null));

        log.info("Usuario creado con id: {}", saved.getId());
        return saved;
    }

    @Override
    public User updateUser(UUID id, UpdateUserCommand command) {
        log.info("Actualizando usuario: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + id));

        if (!user.getEmail().equals(command.email()) && userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException("Email ya existe: " + command.email());
        }

        userDomainService.validateEmail(command.email());
        user.update(command.email(), command.firstName(), command.lastName(),
                command.documentTypeId(), command.documentNumber(), command.updatedBy());

        User saved = userRepository.save(user);

        auditLogRepository.save(AuditLog.create(
                command.updatedBy(), null, "User", id,
                AuditAction.USER_UPDATED, null, null));

        log.info("Usuario actualizado: {}", id);
        return saved;
    }

    @Override
    public void deleteUser(UUID id) {
        log.info("Eliminando usuario: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + id));

        user.softDelete();
        userRepository.save(user);

        auditLogRepository.save(AuditLog.create(
                null, null, "User", id,
                AuditAction.USER_DELETED, null, null));

        log.info("Usuario eliminado (soft delete): {}", id);
    }

    @Override
    public void unlockUser(UUID id) {
        log.info("Desbloqueando usuario: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + id));

        user.unlock();
        userRepository.save(user);

        auditLogRepository.save(AuditLog.create(
                null, null, "User", id,
                AuditAction.ACCOUNT_UNLOCKED, null, null));

        log.info("Usuario desbloqueado: {}", id);
    }

    @Override
    public void changePassword(UUID id, ChangePasswordCommand command) {
        log.info("Cambio de contraseña para usuario: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado: " + id));

        if (!passwordEncoder.matches(command.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Contraseña actual incorrecta");
        }

        userDomainService.validatePassword(command.newPassword());
        String newHash = passwordEncoder.encode(command.newPassword());
        user.updatePasswordHash(newHash, command.requestedBy());
        userRepository.save(user);

        log.info("Contraseña actualizada para usuario: {}", id);
    }

    @Override
    public void assignRoles(UUID userId, List<UUID> roleIds) {
        log.info("Asignando {} roles al usuario: {}", roleIds.size(), userId);

        List<Role> roles = roleRepository.findByIds(roleIds);
        if (roles.size() != roleIds.size()) {
            throw new RoleNotFoundException("Uno o más roles no encontrados");
        }

        log.info("Roles asignados al usuario: {}", userId);
    }
}
