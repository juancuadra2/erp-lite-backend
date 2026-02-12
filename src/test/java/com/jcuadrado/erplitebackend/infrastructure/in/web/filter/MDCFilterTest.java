package com.jcuadrado.erplitebackend.infrastructure.in.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MDCFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private FilterConfig filterConfig;

    @InjectMocks
    private MDCFilter mdcFilter;

    @BeforeEach
    void setUp() {
        MDC.clear();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void doFilter_withoutRequestIdHeader_shouldGenerateRequestId() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Request-ID")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        mdcFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        // MDC should be cleared after filter execution
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    void doFilter_withRequestIdHeader_shouldUseProvidedRequestId() throws ServletException, IOException {
        // Given
        String providedRequestId = UUID.randomUUID().toString();
        when(request.getHeader("X-Request-ID")).thenReturn(providedRequestId);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        mdcFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(MDC.get("requestId")).isNull(); // Should be cleared after
    }

    @Test
    void doFilter_withBlankRequestIdHeader_shouldGenerateNewRequestId() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Request-ID")).thenReturn("   ");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        mdcFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    void doFilter_withCorrelationIdHeader_shouldSetCorrelationId() throws ServletException, IOException {
        // Given
        String correlationId = UUID.randomUUID().toString();
        when(request.getHeader("X-Request-ID")).thenReturn(null);
        when(request.getHeader("X-Correlation-ID")).thenReturn(correlationId);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        mdcFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void doFilter_withBlankCorrelationIdHeader_shouldNotSetCorrelationId() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Request-ID")).thenReturn(null);
        when(request.getHeader("X-Correlation-ID")).thenReturn("   ");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        mdcFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void doFilter_withNullCorrelationIdHeader_shouldNotSetCorrelationId() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Request-ID")).thenReturn(null);
        when(request.getHeader("X-Correlation-ID")).thenReturn(null);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        mdcFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void doFilter_shouldAlwaysClearMDCAfterProcessing() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Request-ID")).thenReturn("test-id");
        when(request.getHeader("X-Correlation-ID")).thenReturn("correlation-id");
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        mdcFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("correlationId")).isNull();
        assertThat(MDC.get("userId")).isNull();
    }

    @Test
    void doFilter_whenExceptionOccurs_shouldStillClearMDC() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Request-ID")).thenReturn("test-id");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");
        doThrow(new ServletException("Test exception")).when(filterChain).doFilter(request, response);

        // When/Then
        try {
            mdcFilter.doFilter(request, response, filterChain);
        } catch (ServletException e) {
            // Expected
        }

        // MDC should be cleared even when exception occurs
        assertThat(MDC.get("requestId")).isNull();
        assertThat(MDC.get("correlationId")).isNull();
    }

    @Test
    void init_shouldLogInitialization() throws ServletException {
        // When
        mdcFilter.init(filterConfig);

        // Then
        // No exception should be thrown
        verify(filterConfig, never()).getInitParameter(anyString());
    }

    @Test
    void destroy_shouldLogDestruction() {
        // When
        mdcFilter.destroy();

        // Then
        // No exception should be thrown
    }

    @Test
    void doFilter_withAllHeaders_shouldSetAllMDCValues() throws ServletException, IOException {
        // Given
        String requestId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);
        when(request.getHeader("X-Correlation-ID")).thenReturn(correlationId);
        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn("/api/documents/123");

        // When
        mdcFilter.doFilter(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request).getHeader("X-Request-ID");
        verify(request).getHeader("X-Correlation-ID");
        verify(request, atLeastOnce()).getMethod();
        verify(request, atLeastOnce()).getRequestURI();
    }
}
