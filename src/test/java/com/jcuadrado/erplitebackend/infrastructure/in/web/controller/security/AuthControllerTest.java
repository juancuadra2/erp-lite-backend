package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.security;

import com.jcuadrado.erplitebackend.application.command.security.LoginCommand;
import com.jcuadrado.erplitebackend.application.command.security.LoginResponse;
import com.jcuadrado.erplitebackend.application.command.security.LogoutCommand;
import com.jcuadrado.erplitebackend.application.command.security.RefreshTokenCommand;
import com.jcuadrado.erplitebackend.application.port.security.AuthUseCase;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.LoginRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.LogoutRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.RefreshTokenRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthUseCase authUseCase;

    @Mock
    private HttpServletRequest httpServletRequest;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authUseCase);
    }

    @Test
    @DisplayName("login should return 200 with valid credentials and provide access and refresh tokens")
    void login_shouldReturn200_withValidCredentials() {
        LoginRequestDto request = new LoginRequestDto("admin", "Secure@1");
        LoginResponse loginResponse = new LoginResponse("jwt-token", "refresh-token-value", 1800L);

        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("TestAgent");
        when(authUseCase.login(any(LoginCommand.class))).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = controller.login(request, httpServletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("jwt-token");
        assertThat(response.getBody().refreshToken()).isEqualTo("refresh-token-value");
        assertThat(response.getBody().expiresIn()).isEqualTo(1800L);
        verify(authUseCase).login(any(LoginCommand.class));
    }

    @Test
    @DisplayName("login should use X-Forwarded-For header when present as client IP")
    void login_shouldUseXForwardedForHeader_whenPresent() {
        LoginRequestDto request = new LoginRequestDto("admin", "Secure@1");
        LoginResponse loginResponse = new LoginResponse("jwt-token", "refresh-value", 1800L);

        when(httpServletRequest.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Mozilla/5.0");
        when(authUseCase.login(any(LoginCommand.class))).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = controller.login(request, httpServletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(authUseCase).login(any(LoginCommand.class));
    }

    @Test
    @DisplayName("refresh should return 200 with a new access token when the refresh token is valid")
    void refresh_shouldReturn200_withValidRefreshToken() {
        RefreshTokenRequestDto request = new RefreshTokenRequestDto("valid-refresh-token");
        LoginResponse loginResponse = new LoginResponse("new-jwt", "new-refresh", 1800L);

        when(authUseCase.refreshToken(any(RefreshTokenCommand.class))).thenReturn(loginResponse);

        ResponseEntity<LoginResponse> response = controller.refresh(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isEqualTo("new-jwt");
        assertThat(response.getBody().refreshToken()).isEqualTo("new-refresh");
        verify(authUseCase).refreshToken(any(RefreshTokenCommand.class));
    }

    @Test
    @DisplayName("logout should return 204 and revoke the refresh token")
    void logout_shouldReturn204() {
        LogoutRequestDto request = new LogoutRequestDto("refresh-token-to-revoke");

        ResponseEntity<Void> response = controller.logout(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(authUseCase).logout(any(LogoutCommand.class));
    }
}
