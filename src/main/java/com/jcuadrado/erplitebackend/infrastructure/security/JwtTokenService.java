package com.jcuadrado.erplitebackend.infrastructure.security;

import com.jcuadrado.erplitebackend.application.port.security.TokenService;
import com.jcuadrado.erplitebackend.domain.model.security.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class JwtTokenService implements TokenService {

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_PERMISSIONS = "permissions";
    private static final String ISSUER = "erp-lite";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration:1800}")
    private long accessTokenExpirationSeconds;

    @Override
    public String generateAccessToken(User user, List<String> roles, List<String> permissions) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpirationSeconds * 1000);

        return Jwts.builder()
                .subject(user.getUsername())
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_PERMISSIONS, permissions)
                .issuedAt(now)
                .expiration(expiration)
                .issuer(ISSUER)
                .signWith(key)
                .compact();
    }

    @Override
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    @Override
    public List<String> extractRoles(String token) {
        return extractStringList(parseClaims(token), CLAIM_ROLES);
    }

    @Override
    public List<String> extractPermissions(String token) {
        return extractStringList(parseClaims(token), CLAIM_PERMISSIONS);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    private Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private List<String> extractStringList(Claims claims, String claimKey) {
        Object value = claims.get(claimKey);
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList();
        }
        return List.of();
    }
}
