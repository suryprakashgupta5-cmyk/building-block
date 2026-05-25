package com.block.security;


import org.slf4j.MDC;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.block.auth.repository.RefreshTokenRepository;
import com.block.tenant.TenantContext;

import java.io.IOException;

/**
 * Intercepts every request, extracts the Bearer token, validates it,
 * sets the Spring SecurityContext, and injects the tenant into TenantContext.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                Long   userId   = jwtTokenProvider.getUserId(token);
                Long   clinicId = jwtTokenProvider.getClinicId(token);
                String role     = jwtTokenProvider.getRole(token);
                String name     = (String) jwtTokenProvider.parseToken(token).get("name");
                
                if (refreshTokenRepository.areAllTokensRevokedForUser(userId)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                // Build principal
                UserPrincipal principal = UserPrincipal.builder()
                        .userId(userId)
                        .clinicId(clinicId)
                        .role(role)
                        .fullName(name)
                        .build();

                // Inject into Spring SecurityContext
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                // Inject into TenantContext (thread-local) — skip for SUPER_ADMIN (no clinic)
                if (clinicId != null) {
                    TenantContext.setClinicId(clinicId);
                }

                // Enrich MDC for log tracing
                if (userId != null)   MDC.put(RequestLoggingFilter.MDC_USER_ID, String.valueOf(userId));
                if (clinicId != null) MDC.put(RequestLoggingFilter.MDC_CLINIC_ID, String.valueOf(clinicId));
                if (role != null)     MDC.put(RequestLoggingFilter.MDC_ROLE, role);
                if (name != null)     MDC.put(RequestLoggingFilter.MDC_USER_EMAIL, name);
            }
        } catch (Exception ex) {
            log.debug("JWT filter error: {}", ex.getMessage());
            // Don't throw — let Spring Security's 401 handler take over
        } finally {
            chain.doFilter(request, response);
            TenantContext.clear(); // always clean up thread-local
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
