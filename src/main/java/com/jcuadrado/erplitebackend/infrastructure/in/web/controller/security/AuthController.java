package com.jcuadrado.erplitebackend.infrastructure.in.web.controller.security;

import com.jcuadrado.erplitebackend.application.command.security.LoginCommand;
import com.jcuadrado.erplitebackend.application.command.security.LoginResponse;
import com.jcuadrado.erplitebackend.application.command.security.LogoutCommand;
import com.jcuadrado.erplitebackend.application.command.security.RefreshTokenCommand;
import com.jcuadrado.erplitebackend.application.port.security.AuthUseCase;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.LoginRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.LogoutRequestDto;
import com.jcuadrado.erplitebackend.infrastructure.in.web.dto.security.RefreshTokenRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "API for authentication and token management")
public class AuthController {

    private final AuthUseCase authUseCase;

    @Operation(summary = "Authenticate user and obtain JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "403", description = "Account locked")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest httpRequest) {
        String ipAddress = resolveClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginCommand command = new LoginCommand(request.username(), request.password(), ipAddress, userAgent);
        LoginResponse response = authUseCase.login(command);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh access token using refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or revoked refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        RefreshTokenCommand command = new RefreshTokenCommand(request.refreshToken());
        LoginResponse response = authUseCase.refreshToken(command);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout and revoke refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout successful"),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequestDto request) {
        LogoutCommand command = new LogoutCommand(request.refreshToken());
        authUseCase.logout(command);
        return ResponseEntity.noContent().build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
