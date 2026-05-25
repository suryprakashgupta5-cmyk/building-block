package com.block.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-ID";
    public static final String MDC_REQUEST_ID    = "requestId";
    public static final String MDC_METHOD        = "method";
    public static final String MDC_URI           = "uri";
    public static final String MDC_CLIENT_IP     = "clientIp";
    public static final String MDC_CLINIC_ID     = "clinicId";
    public static final String MDC_USER_ID       = "userId";
    public static final String MDC_USER_EMAIL    = "userEmail";
    public static final String MDC_ROLE          = "role";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String incoming = request.getHeader(REQUEST_ID_HEADER);
        String requestId = StringUtils.hasText(incoming) ? incoming : UUID.randomUUID().toString();

        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_METHOD, request.getMethod());
        MDC.put(MDC_URI, request.getRequestURI());
        MDC.put(MDC_CLIENT_IP, resolveClientIp(request));

        response.setHeader(REQUEST_ID_HEADER, requestId);

        long start = System.currentTimeMillis();
        log.info("--> {} {}", request.getMethod(), request.getRequestURI());

        try {
            chain.doFilter(request, response);
        } finally {
            long elapsed = System.currentTimeMillis() - start;
            log.info("<-- {} {} status={} {}ms",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), elapsed);
            MDC.clear();
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        String xri = request.getHeader("X-Real-IP");
        return StringUtils.hasText(xri) ? xri : request.getRemoteAddr();
    }
}
