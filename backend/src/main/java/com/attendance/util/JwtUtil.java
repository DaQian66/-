package com.attendance.util;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;

    public String generateToken(Integer userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Claims parseToken(String token) {
        try {
            return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public Integer getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        return (Integer) claims.get("userId");
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        return (String) claims.get("role");
    }

    public boolean isTokenExpired(String token) {
        return parseToken(token) == null;
    }

    public String getHeader() { return header; }
    public String getTokenPrefix() { return tokenPrefix; }
}