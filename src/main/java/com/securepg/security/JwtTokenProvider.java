package com.securepg.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        byte[] keyBytes = secret.length() >= 32
                ? secret.getBytes(StandardCharsets.UTF_8)
                : Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationMs = expirationMs;
    }

    public String generateToken(JwtUserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        var builder = Jwts.builder()
                .subject(principal.getEmail())
                .claim("userId", principal.getUserId().toString())
                .claim("role", principal.getRole().name())
                .issuedAt(now)
                .expiration(expiry);

        if (principal.getTenantProfileId() != null) {
            builder.claim("tenantProfileId", principal.getTenantProfileId().toString());
        }

        return builder.signWith(secretKey).compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public JwtUserPrincipal toPrincipal(Claims claims) {
        return JwtUserPrincipal.builder()
                .email(claims.getSubject())
                .userId(UUID.fromString(claims.get("userId", String.class)))
                .role(com.securepg.entity.Role.valueOf(claims.get("role", String.class)))
                .tenantProfileId(claims.get("tenantProfileId") != null
                        ? UUID.fromString(claims.get("tenantProfileId", String.class))
                        : null)
                .build();
    }
}
