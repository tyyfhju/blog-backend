package com.zjy.blog.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt.secret}")
    private String secret;
    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(String email, String role, String sessionId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        claims.setId(sessionId);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody();
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    /**
     * 邮件退订链接用的长期令牌，只包含用户 id 和用途，不能当登录 token 用。
     */
    public String generateUnsubscribeToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + 365L * 24 * 60 * 60 * 1000);
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("purpose", "unsubscribe");
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Long parseUnsubscribeUserId(String token) {
        Claims claims = parseClaims(token);
        if (!"unsubscribe".equals(claims.get("purpose"))) {
            throw new IllegalArgumentException("Not an unsubscribe token");
        }
        return Long.valueOf(claims.getSubject());
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public String getRoleFromToken(String token) {
        return (String) Jwts.parserBuilder().setSigningKey(getSigningKey()).build()
                .parseClaimsJws(token).getBody().get("role");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}