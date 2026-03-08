package com.betting.settlement.infra.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private final SecretKey secretKey;

    public JwtUtil(
            @Value("${app.jwt.secret:dGhpcy1pcy1hLXZlcnktc2VjcmV0LWtleS10aGF0LWlzLWF0LWxlYXN0LTMyLWNoYXJhY3RlcnMtbG9uZw==}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String extractTenantId(String token) { return parseClaims(token).get("tenant_id", String.class); }
    public String extractSubject(String token) { return parseClaims(token).getSubject(); }
    public boolean validateToken(String token) {
        try { parseClaims(token); return true; } 
        catch (JwtException | IllegalArgumentException e) { log.debug("Invalid JWT: {}", e.getMessage()); return false; }
    }
    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }
}
