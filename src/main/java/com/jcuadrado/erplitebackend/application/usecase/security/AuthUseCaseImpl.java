package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.command.security.AuditLogFilter;
import com.jcuadrado.erplitebackend.application.command.security.LoginCommand;
import com.jcuadrado.erplitebackend.application.command.security.LoginResponse;
import com.jcuadrado.erplitebackend.application.command.security.LogoutCommand;
import com.jcuadrado.erplitebackend.application.command.security.RefreshTokenCommand;
import com.jcuadrado.erplitebackend.application.port.security.AuthUseCase;
import com.jcuadrado.erplitebackend.application.port.security.PasswordEncoder;
import com.jcuadrado.erplitebackend.application.port.security.TokenService;
import com.jcuadrado.erplitebackend.domain.exception.security.AccountLockedException;
import com.jcuadrado.erplitebackend.domain.exception.security.InvalidCredentialsException;
import com.jcuadrado.erplitebackend.domain.exception.security.InvalidRefreshTokenException;
import com.jcuadrado.erplitebackend.domain.model.security.AuditAction;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.domain.model.security.Permission;
import com.jcuadrado.erplitebackend.domain.model.security.RefreshToken;
import com.jcuadrado.erplitebackend.domain.model.security.Role;
import com.jcuadrado.erplitebackend.domain.model.security.User;
import com.jcuadrado.erplitebackend.domain.port.security.AuditLogRepository;
import com.jcuadrado.erplitebackend.domain.port.security.PermissionRepository;
import com.jcuadrado.erplitebackend.domain.port.security.RefreshTokenRepository;
import com.jcuadrado.erplitebackend.domain.port.security.RoleRepository;
import com.jcuadrado.erplitebackend.domain.port.security.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class AuthUseCaseImpl implements AuthUseCase {

    private static final int REFRESH_TOKEN_DAYS = 7;
    private static final long ACCESS_TOKEN_EXPIRES_IN = 1800L;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    public LoginResponse login(LoginCommand command) {
        log.info("Intento de login para usuario: {}", command.username());

        User user = userRepository.findByUsername(command.username())
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado: {}", command.username());
                    return new InvalidCredentialsException("Credenciales inválidas");
                });

        if (user.isDeleted()) {
            log.warn("Intento de login en cuenta eliminada: {}", command.username());
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        if (user.isLocked()) {
            log.warn("Intento de login en cuenta bloqueada: {}", command.username());
            throw new AccountLockedException("Cuenta bloqueada. Contacte al administrador");
        }

        if (!user.isActive() && !user.isLocked()) {
            log.warn("Intento de login en cuenta inactiva: {}", command.username());
            throw new AccountLockedException("Cuenta bloqueada. Contacte al administrador");
        }

        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            user.incrementFailedAttempts();
            userRepository.save(user);

            auditLogRepository.save(AuditLog.create(
                    user.getId(), user.getUsername(), "User", user.getId(),
                    AuditAction.LOGIN_FAILED, command.ipAddress(), command.userAgent()));

            if (user.isLocked()) {
                log.warn("Cuenta bloqueada por múltiples intentos fallidos: {}", command.username());
                auditLogRepository.save(AuditLog.create(
                        user.getId(), user.getUsername(), "User", user.getId(),
                        AuditAction.ACCOUNT_LOCKED, command.ipAddress(), command.userAgent()));
                throw new AccountLockedException(
                        "Cuenta bloqueada por múltiples intentos fallidos. Contacte al administrador");
            }

            log.warn("Contraseña incorrecta para usuario: {}", command.username());
            throw new InvalidCredentialsException("Credenciales inválidas");
        }

        user.recordSuccessfulLogin();
        userRepository.save(user);

        List<String> roles = buildRoleNames(user.getId());
        List<String> permissions = buildPermissionStrings(user.getId());

        String accessToken = tokenService.generateAccessToken(user, roles, permissions);
        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.create(user.getId(), refreshTokenValue, REFRESH_TOKEN_DAYS);
        refreshTokenRepository.save(refreshToken);

        auditLogRepository.save(AuditLog.create(
                user.getId(), user.getUsername(), "User", user.getId(),
                AuditAction.LOGIN, command.ipAddress(), command.userAgent()));

        log.info("Login exitoso para usuario: {}", command.username());
        return new LoginResponse(accessToken, refreshTokenValue, ACCESS_TOKEN_EXPIRES_IN);
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenCommand command) {
        log.debug("Solicitud de refresh token");

        RefreshToken storedToken = refreshTokenRepository.findByToken(command.refreshToken())
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token inválido o revocado"));

        if (!storedToken.isValid()) {
            throw new InvalidRefreshTokenException("Refresh token inválido o revocado");
        }

        storedToken.revoke();
        refreshTokenRepository.save(storedToken);

        User user = userRepository.findById(storedToken.getUserId())
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token inválido o revocado"));

        List<String> roles = buildRoleNames(user.getId());
        List<String> permissions = buildPermissionStrings(user.getId());

        String newAccessToken = tokenService.generateAccessToken(user, roles, permissions);
        String newRefreshTokenValue = UUID.randomUUID().toString();
        RefreshToken newRefreshToken = RefreshToken.create(user.getId(), newRefreshTokenValue, REFRESH_TOKEN_DAYS);
        refreshTokenRepository.save(newRefreshToken);

        log.debug("Refresh token rotado para usuario: {}", user.getUsername());
        return new LoginResponse(newAccessToken, newRefreshTokenValue, ACCESS_TOKEN_EXPIRES_IN);
    }

    @Override
    public void logout(LogoutCommand command) {
        log.debug("Solicitud de logout");

        RefreshToken storedToken = refreshTokenRepository.findByToken(command.refreshToken())
                .orElse(null);

        if (storedToken != null) {
            storedToken.revoke();
            refreshTokenRepository.save(storedToken);

            userRepository.findById(storedToken.getUserId()).ifPresent(user ->
                    auditLogRepository.save(AuditLog.create(
                            user.getId(), user.getUsername(), "User", user.getId(),
                            AuditAction.LOGOUT, null, null)));
        }

        log.debug("Logout completado");
    }

    private List<String> buildRoleNames(UUID userId) {
        return roleRepository.findByUserId(userId).stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    private List<String> buildPermissionStrings(UUID userId) {
        return permissionRepository.findByUserId(userId).stream()
                .map(p -> p.getEntity() + ":" + p.getAction().name())
                .collect(Collectors.toList());
    }
}
