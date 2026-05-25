package com.block.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.block.config.AppProperties;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final AppProperties appProperties;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = appProperties.getJwt().getSecret()
                .getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // ── Generate access token ────────────────────────────────────
    public String generateAccessToken(Long userId, Long clinicId, String role, String name) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + appProperties.getJwt().getAccessTokenExpiryMs());

        var claims = new java.util.HashMap<String, Object>();
        if (clinicId != null) {
            claims.put("clinicId", clinicId);
        }
        claims.put("role", role);
        claims.put("name", name);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(claims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    // ── Parse & validate ─────────────────────────────────────────
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.debug("JWT expired: {}", ex.getMessage());
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT: {}", ex.getMessage());
        }
        return false;
    }

    // ── Claim extractors ─────────────────────────────────────────
    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    public Long getClinicId(String token) {
        Object v = parseToken(token).get("clinicId");
        if (v == null) return null;
        return v instanceof Integer ? ((Integer) v).longValue() : (Long) v;
    }

    public String getRole(String token) {
        return (String) parseToken(token).get("role");
    }
}
