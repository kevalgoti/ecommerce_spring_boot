package com.ecommerce.ecommerce.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static final String SECRET_KEY = "your-256-bit-secret-your-256-bit-secret";
    private static final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours

    // Simulated user-role mapping (replace with database logic)
    private static final Map<String, String> userRoles = new HashMap<>();

    static {
        userRoles.put("sellerUser", "SELLER");
        userRoles.put("buyerUser", "BUYER");
    }

    // Generate JWT token
    public static String generateToken(String username, String role) {
        userRoles.put(username, role); // Store role for quick lookup (replace with DB lookup)

        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Validate JWT token
    public static boolean validateToken(String token, String username) {
        try {
            Claims claims = parseToken(token);
            return claims.getSubject().equals(username) && !isTokenExpired(claims);
        } catch (JwtException e) {
            return false;
        }
    }

    // Extract username from JWT token
    public static String extractUsername(String token) {
        try {
            return parseToken(token).getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    // Extract user role from JWT token
    public static String extractUserRole(String token) {
        try {
            return parseToken(token).get("role", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    // Extract role from username (used when token is not available)
    public static String extractUserRoleFromUsername(String username) {
        return userRoles.getOrDefault(username, "UNKNOWN"); // Fetch from DB instead
    }

    // Parse JWT token and return claims
    private static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if token is expired
    private static boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
