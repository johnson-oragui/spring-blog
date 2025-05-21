package com.johnson.utilities.security;

import java.security.Key;
import java.util.Date;

import com.johnson.utilities.config.ConfigUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {
  private static final Key KEY = Keys.hmacShaKeyFor(ConfigUtils.JWT_SECRET.getBytes());

  public static String generateAccessToken(String userId, String email, String jti, String deviceId) {
    return Jwts.builder().setSubject(userId).claim("email", email).claim("jti", jti).claim("deviceId", deviceId)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + Long.parseLong(ConfigUtils.JWT_EXPIRATION) * 1000L))
        .signWith(KEY, SignatureAlgorithm.HS256).compact();
  }

  public static Jws<Claims> validateToken(String token) throws JwtException {
    return Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token);
  }

  public static String extractUserId(String token) {
    return validateToken(token).getBody().getSubject();
  }

  public static String extractEmail(String token) {
    return validateToken(token).getBody().get("email", String.class);
  }
}
