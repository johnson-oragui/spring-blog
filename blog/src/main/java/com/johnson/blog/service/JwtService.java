package com.johnson.blog.service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.johnson.blog.filters.CustomUserDetails;
import com.johnson.database.model.UserSessionModel;
import com.johnson.database.repository.UserSessionRepository;
import com.johnson.utilities.config.ConfigUtils;
import com.johnson.utilities.exceptions.UnauthorizedException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
  private final UserSessionRepository userSessionRepository;

  @Value("${application.security.jwt.secret-key}")
  private String secretKey = ConfigUtils.JWT_SECRET;

  @Value("${application.security.jwt.expiration}")
  private long jwtExpiration = Long.parseLong(ConfigUtils.JWT_EXPIRATION);

  @Value("${application.security.jwt.refresh-token.secret-key}")
  private String refreshSecretKey = ConfigUtils.JWT_REFRESH_SECRET;

  @Value("${application.security.jwt.refresh-token.expiration}")
  private long refreshExpiration = Long.parseLong(ConfigUtils.JWT_REFRESH_EXPIRATION);

  private final Key JWTKEY = Keys.hmacShaKeyFor(secretKey.getBytes());
  private final Key JWTREFRESHKEY = Keys.hmacShaKeyFor(refreshSecretKey.getBytes());

  public JwtService(UserSessionRepository userSessionRepository) {
    this.userSessionRepository = userSessionRepository;
  }

  /*
   * Builds Token for access and refresh.
   * type of token is decided by the tokenType parameter.
   * must include tokenType in the extraClaims params.
   */
  public String buildToken(Map<String, Object> extraClaims, CustomUserDetails customUserDetails, String tokenType) {

    Long expiresIn;
    Key currentKey;
    if (tokenType.equals("access")) {
      expiresIn = Long.parseLong(ConfigUtils.JWT_EXPIRATION);
      currentKey = JWTKEY;
    } else {
      expiresIn = Long.parseLong(ConfigUtils.JWT_REFRESH_EXPIRATION);
      currentKey = JWTREFRESHKEY;
    }
    System.out.println("username in use>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + customUserDetails.getUsername());

    return Jwts.builder()
        .setSubject(customUserDetails.getUsername()) // email in this case
        .setClaims(extraClaims)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expiresIn * 1000L))
        .signWith(currentKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public Claims validateToken(String token, String tokenType) throws JwtException {
    try {
      Key currentKey;
      if (tokenType.equals("access")) {
        currentKey = JWTKEY;
      } else {
        currentKey = JWTREFRESHKEY;
      }
      return Jwts
          .parserBuilder()
          .setSigningKey(currentKey)
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (MalformedJwtException e) {
      throw new UnauthorizedException(e.getMessage());
    }
  }

  public boolean isTokenValid(String token, UserDetails userDetails, String TokenType) {
    final String email = extractClaim(token, "email", TokenType);
    return (email.equals(userDetails.getUsername())) && !isTokenExpired(token, TokenType);
  }

  private boolean isTokenExpired(String token, String tokenType) {
    return extractExpiration(token, tokenType).before(new Date());
  }

  public boolean isTokenRevoked(String token, String TokenType) {
    String deviceId = extractClaim(token, "deviceId", TokenType);
    String userId = extractClaim(token, "userId", TokenType);
    String jti = extractClaim(token, "jti", TokenType);

    return userSessionRepository
        .findUserSession(jti, userId, deviceId).map(UserSessionModel::isLoggedOut)
        .orElse(true);
  }

  private Date extractExpiration(String token, String tokenType) {
    return validateToken(token, tokenType).getExpiration();
  }

  public String extractClaim(String token, String claimName, String tokenType) {
    return validateToken(token, tokenType).get(claimName, String.class);
  }

  public String extractUserId(String token, String tokenType) {
    return validateToken(token, tokenType).getSubject();
  }

}
