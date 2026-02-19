package com.jcuadrado.erplitebackend.application.usecase.security;

import com.jcuadrado.erplitebackend.application.command.security.LoginCommand;
import com.jcuadrado.erplitebackend.application.command.security.LoginResponse;
import com.jcuadrado.erplitebackend.application.command.security.LogoutCommand;
import com.jcuadrado.erplitebackend.application.command.security.RefreshTokenCommand;
import com.jcuadrado.erplitebackend.application.port.security.PasswordEncoder;
import com.jcuadrado.erplitebackend.application.port.security.TokenService;
import com.jcuadrado.erplitebackend.domain.exception.security.AccountLockedException;
import com.jcuadrado.erplitebackend.domain.exception.security.InvalidCredentialsException;
import com.jcuadrado.erplitebackend.domain.exception.security.InvalidRefreshTokenException;
import com.jcuadrado.erplitebackend.domain.model.security.AuditLog;
import com.jcuadrado.erplitebackend.domain.model.security.RefreshToken;
import com.jcuadrado.erplitebackend.domain.model.security.User;
import com.jcuadrado.erplitebackend.domain.port.security.AuditLogRepository;
import com.jcuadrado.erplitebackend.domain.port.security.PermissionRepository;
import com.jcuadrado.erplitebackend.domain.port.security.RefreshTokenRepository;
import com.jcuadrado.erplitebackend.domain.port.security.RoleRepository;
import com.jcuadrado.erplitebackend.domain.port.security.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    private AuthUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new AuthUseCaseImpl(
                userRepository,
                roleRepository,
                permissionRepository,
                refreshTokenRepository,
                auditLogRepository,
                passwordEncoder,
                tokenService);
    }

    @Test
    @DisplayName("login should return tokens on success when credentials are valid")
    void login_shouldReturnTokensOnSuccess() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("admin")
                .passwordHash("hashed")
                .active(true)
                .failedAttempts(0)
                .build();

        LoginCommand command = new LoginCommand("admin", "plain", "127.0.0.1", "TestAgent");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plain", "hashed")).thenReturn(true);
        when(roleRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(permissionRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(tokenService.generateAccessToken(any(User.class), anyList(), anyList())).thenReturn("jwt-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        LoginResponse response = useCase.login(command);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.expiresIn()).isEqualTo(1800L);
        verify(userRepository).save(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("login should throw InvalidCredentialsException and increment failedAttempts when password is wrong")
    void login_shouldThrowInvalidCredentials_whenPasswordWrong() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("admin")
                .passwordHash("hashed")
                .active(true)
                .failedAttempts(0)
                .build();

        LoginCommand command = new LoginCommand("admin", "wrong", "127.0.0.1", "TestAgent");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> useCase.login(command))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(user.getFailedAttempts()).isEqualTo(1);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("login should throw AccountLockedException when user is already locked (active=false, failedAttempts>=5)")
    void login_shouldThrowAccountLocked_whenUserIsLocked() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .username("admin")
                .passwordHash("hashed")
                .active(false)
                .failedAttempts(5)
                .build();

        LoginCommand command = new LoginCommand("admin", "any", "127.0.0.1", "TestAgent");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> useCase.login(command))
                .isInstanceOf(AccountLockedException.class);

        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("login should throw InvalidCredentialsException when user is not found")
    void login_shouldThrowInvalidCredentials_whenUserNotFound() {
        LoginCommand command = new LoginCommand("unknown", "pass", "127.0.0.1", "TestAgent");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.login(command))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    @DisplayName("refreshToken should rotate the refresh token and return new tokens when the token is valid")
    void refreshToken_shouldRotateRefreshToken() {
        UUID userId = UUID.randomUUID();
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken storedToken = RefreshToken.create(userId, tokenValue, 7);

        User user = User.builder()
                .id(userId)
                .username("admin")
                .active(true)
                .failedAttempts(0)
                .build();

        RefreshTokenCommand command = new RefreshTokenCommand(tokenValue);

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(storedToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(permissionRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(tokenService.generateAccessToken(any(User.class), anyList(), anyList())).thenReturn("new-jwt");

        LoginResponse response = useCase.refreshToken(command);

        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("new-jwt");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(storedToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(storedToken);
    }

    @Test
    @DisplayName("refreshToken should throw InvalidRefreshTokenException when token is not found")
    void refreshToken_shouldThrowInvalidRefreshToken_whenTokenNotFound() {
        RefreshTokenCommand command = new RefreshTokenCommand("nonexistent-token");

        when(refreshTokenRepository.findByToken("nonexistent-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.refreshToken(command))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("refreshToken should throw InvalidRefreshTokenException when token is already revoked")
    void refreshToken_shouldThrowInvalidRefreshToken_whenTokenIsRevoked() {
        UUID userId = UUID.randomUUID();
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken storedToken = RefreshToken.create(userId, tokenValue, 7);
        storedToken.revoke();

        RefreshTokenCommand command = new RefreshTokenCommand(tokenValue);

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> useCase.refreshToken(command))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    @DisplayName("logout should revoke the refresh token and save an audit log")
    void logout_shouldRevokeRefreshToken() {
        UUID userId = UUID.randomUUID();
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken storedToken = RefreshToken.create(userId, tokenValue, 7);

        User user = User.builder()
                .id(userId)
                .username("admin")
                .active(true)
                .failedAttempts(0)
                .build();

        LogoutCommand command = new LogoutCommand(tokenValue);

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(storedToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.logout(command);

        assertThat(storedToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(storedToken);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("logout should complete without error when refresh token is not found")
    void logout_shouldCompleteWithoutError_whenTokenNotFound() {
        LogoutCommand command = new LogoutCommand("nonexistent-token");

        when(refreshTokenRepository.findByToken("nonexistent-token")).thenReturn(Optional.empty());

        useCase.logout(command);

        verify(refreshTokenRepository, never()).save(any());
        verify(auditLogRepository, never()).save(any());
    }
}
