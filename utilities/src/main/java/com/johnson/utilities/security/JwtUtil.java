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
  private final Key KEY = Keys.hmacShaKeyFor(ConfigUtils.JWT_SECRET.getBytes());

  public String generateToken(String userId, String email, String jti, String deviceId, String tokenType) {
    Long expiresIn;
    if (tokenType.equals("access")) {
      expiresIn = Long.parseLong(ConfigUtils.JWT_EXPIRATION);
    } else {
      expiresIn = Long.parseLong(ConfigUtils.JWT_REFRESH_EXPIRATION);
    }

    return Jwts.builder()
        .setSubject(userId)
        .claim("email", email)
        .claim("jti", jti)
        .claim("deviceId", deviceId)
        .claim("tokenType", tokenType)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiresIn * 1000L))
        .signWith(KEY, SignatureAlgorithm.HS256).compact();
  }

  public Jws<Claims> validateToken(String token) throws JwtException {
    return Jwts.parserBuilder().setSigningKey(KEY).build().parseClaimsJws(token);
  }

  public String extractUserId(String token) {
    return validateToken(token).getBody().getSubject();
  }

  public String extractClaim(String token, String claimName) {
    return validateToken(token).getBody().get(claimName, String.class);
  }
}
