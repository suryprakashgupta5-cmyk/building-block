package com.block.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.MediaType;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.block.security.JwtAuthFilter;
import com.block.security.RequestLoggingFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter        jwtAuthFilter;
    private final RequestLoggingFilter requestLoggingFilter;
    private final AppProperties        appProperties;

    // ── Public endpoints ─────────────────────────────────────────
    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/**",
            "/api/v1/subscription/plans",
            "/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health",
            "/actuator/health/**",    // k8s /liveness + /readiness probes
            "/uploads/**"             // user-uploaded assets (clinic logos) — public read
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.getWriter().write(
                            "{\"success\":false,\"message\":\"Access denied\",\"error\":{\"code\":\"ACCESS_DENIED\",\"message\":\"You do not have permission to access this resource\"}}");
                    }))
            .authorizeHttpRequests(auth -> auth
                    // Authenticated auth endpoints — must come BEFORE the /auth/** permitAll
                    .requestMatchers("/api/v1/auth/change-password",
                                     "/api/v1/auth/setup-status",
                                     "/api/v1/auth/me",
                                     "/api/v1/auth/logout").authenticated()
                    .requestMatchers(PUBLIC_PATHS).permitAll()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // SUPER_ADMIN platform panel
                    .requestMatchers("/api/v1/platform/**").hasRole("SUPER_ADMIN")
                    // Role-specific restrictions
                    // Doctor-list is needed by any clinic user (filters, dropdowns); must come BEFORE the generic /users/** rule
                    .requestMatchers(HttpMethod.GET, "/api/v1/users/doctors").hasAnyRole("DOCTOR", "RECEPTION", "ADMIN", "DOCTOR_ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "DOCTOR_ADMIN", "SUPER_ADMIN")
                    .requestMatchers("/api/v1/analytics/**").hasAnyRole("DOCTOR", "ADMIN", "DOCTOR_ADMIN")
                    // Consultation writes restricted to clinical/managerial roles via @PreAuthorize on controller;
                    // reads (GET) open to all authenticated users for patient detail view
                    .requestMatchers(HttpMethod.POST, "/api/v1/consultations/**").hasAnyRole("DOCTOR", "ADMIN", "DOCTOR_ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/api/v1/consultations/**").hasAnyRole("DOCTOR", "ADMIN", "DOCTOR_ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/v1/consultations/**").authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/v1/medicines/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/v1/medicines/**").hasAnyRole("DOCTOR", "ADMIN", "DOCTOR_ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/medicines/**").hasAnyRole("DOCTOR", "ADMIN", "DOCTOR_ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/medicines/**").hasAnyRole("DOCTOR", "ADMIN", "DOCTOR_ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/settings/**").hasAnyRole("DOCTOR", "ADMIN", "DOCTOR_ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/v1/settings/**").authenticated()
                    // Clinic-facing support & announcements
                    .requestMatchers("/api/v1/support/**").hasAnyRole("ADMIN", "DOCTOR_ADMIN")
                    .requestMatchers("/api/v1/announcements/**").authenticated()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(requestLoggingFilter, JwtAuthFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(appProperties.getCors().getAllowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "X-Request-ID"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        config.setAllowedOriginPatterns(List.of("http://192.168.1.*:4200", "http://localhost:4200",
        		"http://mediomenadmin.appdemo.in","http://mediomenadmin.appdemo.in/","http://mediomen.appdemo.in","http://mediomen.appdemo.in/"));
	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
   
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
