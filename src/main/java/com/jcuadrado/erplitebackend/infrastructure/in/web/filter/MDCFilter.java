package com.jcuadrado.erplitebackend.infrastructure.in.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to populate MDC (Mapped Diagnostic Context) with contextual information
 * for request tracing and logging.
 *
 * This filter adds:
 * - requestId: Unique identifier for each request
 * - correlationId: For distributed tracing (if present in header)
 * - userId: User identifier (will be populated when authentication is implemented)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class MDCFilter implements Filter {

    private static final String REQUEST_ID = "requestId";
    private static final String CORRELATION_ID = "correlationId";
    //private static final String USER_ID = "userId";

    private static final String HEADER_REQUEST_ID = "X-Request-ID";
    private static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        try {
            // Generate or retrieve requestId from header
            String requestId = httpRequest.getHeader(HEADER_REQUEST_ID);
            if (requestId == null || requestId.isBlank()) {
                requestId = UUID.randomUUID().toString();
            }
            MDC.put(REQUEST_ID, requestId);

            // Retrieve correlationId if exists (for distributed tracing)
            String correlationId = httpRequest.getHeader(HEADER_CORRELATION_ID);
            if (correlationId != null && !correlationId.isBlank()) {
                MDC.put(CORRELATION_ID, correlationId);
            }

            // TODO: Extract userId from SecurityContext when authentication is implemented
            // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // if (auth != null && auth.isAuthenticated()) {
            //     String userId = extractUserIdFromAuth(auth);
            //     MDC.put(USER_ID, userId);
            // }

            log.info("Request started: {} {} [requestId={}]",
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                requestId);

            // Continue with the filter chain
            chain.doFilter(request, response);

            log.info("Request completed: {} {}",
                httpRequest.getMethod(),
                httpRequest.getRequestURI());

        } finally {
            // Always clear MDC to prevent memory leaks
            MDC.clear();
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("MDC Filter initialized");
    }

    @Override
    public void destroy() {
        log.info("MDC Filter destroyed");
    }

    /**
     * Extract user ID from authentication object.
     * This will be implemented when authentication module is ready.
     *
     * @param auth Authentication object
     * @return User ID as string
     */
    // private String extractUserIdFromAuth(Authentication auth) {
    //     // Implementation depends on JWT structure
    //     return auth.getName(); // Placeholder
    // }
}

