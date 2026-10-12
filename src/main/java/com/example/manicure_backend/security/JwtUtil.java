package com.example.manicure_backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    private Key key;

    @PostConstruct
    public void init() {
        byte[] keyBytes;
        try {
            // Tenta usar Base64 (recomendado para chaves fortes em produção)
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception e) {
            // Fallback seguro caso seja uma string comum
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        // Nota de Segurança: A chave no Discloud/ambiente local deve ter no mínimo 32 caracteres.
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // GERAÇÃO DE TOKEN SIMPLES COM EMAIL
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // EXTRAIR EMAIL
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // VALIDAR TOKEN
    public boolean validateToken(String token) {
        try {
            extractEmail(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}