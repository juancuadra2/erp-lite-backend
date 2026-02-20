package com.jcuadrado.erplitebackend.infrastructure.in.web.filter;

import com.jcuadrado.erplitebackend.application.port.security.TokenService;
import com.jcuadrado.erplitebackend.application.port.security.UserPermissionsUseCase;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserPermissionsUseCase userPermissionsUseCase;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(tokenService, userPermissionsUseCase);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal should pass through when Authorization header is absent")
    void doFilterInternal_shouldPassThrough_whenNoAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(tokenService, never()).validateToken(org.mockito.ArgumentMatchers.any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal should pass through when Authorization header does not start with Bearer")
    void doFilterInternal_shouldPassThrough_whenHeaderNotBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(tokenService, never()).validateToken(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("doFilterInternal should pass through when token is invalid")
    void doFilterInternal_shouldPassThrough_whenTokenIsInvalid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(tokenService.validateToken("invalid-token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(tokenService, never()).extractUsername(org.mockito.ArgumentMatchers.any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal should set authentication with roles from JWT and permissions from DB when token is valid")
    void doFilterInternal_shouldSetAuthentication_whenTokenIsValid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(tokenService.validateToken("valid-token")).thenReturn(true);
        when(tokenService.extractUsername("valid-token")).thenReturn("alice");
        when(tokenService.extractRoles("valid-token")).thenReturn(List.of("ADMIN"));
        when(userPermissionsUseCase.getPermissionStrings("alice")).thenReturn(List.of("WAREHOUSE:READ", "WAREHOUSE:CREATE"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo("alice");
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_ADMIN", "WAREHOUSE:READ", "WAREHOUSE:CREATE");
    }

    @Test
    @DisplayName("doFilterInternal should set authentication with empty authorities when token has no roles and user has no permissions")
    void doFilterInternal_shouldSetAuthentication_withNoAuthorities_whenTokenHasNone() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer minimal-token");
        when(tokenService.validateToken("minimal-token")).thenReturn(true);
        when(tokenService.extractUsername("minimal-token")).thenReturn("bob");
        when(tokenService.extractRoles("minimal-token")).thenReturn(List.of());
        when(userPermissionsUseCase.getPermissionStrings("bob")).thenReturn(List.of());

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("doFilterInternal should continue filter chain when extractUsername throws an exception")
    void doFilterInternal_shouldContinueFilterChain_whenExtractUsernameThrows() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer problem-token");
        when(tokenService.validateToken("problem-token")).thenReturn(true);
        when(tokenService.extractUsername("problem-token")).thenThrow(new RuntimeException("Parse error"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
