package com.johnson.blog.filters;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.johnson.blog.service.CustomUserDetailsService;
import com.johnson.blog.service.JwtService;

import io.jsonwebtoken.JwtException;
import io.micrometer.common.lang.NonNull;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * JWT Authentication Filter that processes every incoming request.
 * 
 * NOTE: This filter executes BEFORE Spring Security's requestMatchers() checks.
 * The PUBLIC_PATHS list provides a performance optimization to skip JWT
 * processing
 * for whitelisted routes, but security is ultimately enforced by Spring
 * Security's
 * authorization rules.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  // Service for JWT operations (token validation, claim extraction)
  private final JwtService jwtService;
  // Service to load user details from database
  private final CustomUserDetailsService customUserDetailsService;
  // Routes that bypass JWT validation completely
  private final List<String> PUBLIC_PATHS = List.of(
      "/api/v1/auth/login",
      "/api/v1/auth/refresh",
      "/api/v1/auth/register");

  public JwtAuthenticationFilter(
      JwtService jwtService,
      CustomUserDetailsService customUserDetailsService) {
    this.jwtService = jwtService;
    this.customUserDetailsService = customUserDetailsService;
  }

  /**
   * Core filter method - executes for every request not excluded by
   * shouldNotFilter()
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    String userAgent = request.getHeader("User-Agent");
    if (userAgent == null) {
      // Write JSON error body
      response.getWriter().write(
          String.format("{\"status\": 400,\"error\": \"Unauthorized\", \"message\": \"%s\"}", "Missing User-Agent"));

      // Ensure error is committed
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing User-Agent");
      return;
    }

    String authHeader = request.getHeader("Authorization");

    // System.out.println(">>>>>>>>>>>>>>>>> authHeader: " + authHeader);

    // Reject requests without Bearer token
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      sendAuthError(response, "Missing Authentication Header");
      return; // Stop filter chain for unauthenticated requests
    }

    String jwtToken = authHeader.substring(7);

    // Validate token and set authentication
    authenticateFromToken(jwtToken, request, response);
    // Proceed to next filter/controller ONLY if token was valid
    filterChain.doFilter(request, response);
  }

  /**
   * Optimization: Skip JWT processing for public routes.
   * NOTE: This is PERFORMANCE-ONLY - security is still enforced by Spring
   * Security's
   * requestMatchers() configuration.
   */
  @Override
  public boolean shouldNotFilter(@NonNull HttpServletRequest request) {
    String uri = request.getRequestURI();
    boolean shouldSkip = PUBLIC_PATHS.stream().anyMatch(uri::startsWith);
    // System.out.println(">>>>>>>>>>>>>>>>>> Checking shouldNotFilter for URI: " +
    // uri + ", result: " + shouldSkip);

    boolean shouldAuth = PUBLIC_PATHS.stream().anyMatch(path -> uri.startsWith(path));
    if (uri == "/") {
      shouldAuth = true;
    }
    return shouldAuth;
  }

  /**
   * Validates JWT and authenticates the user if valid
   */
  private void authenticateFromToken(String jwt, HttpServletRequest request, HttpServletResponse response)
      throws IOException, JwtException {
    // Extract email claim from JWT
    String email = jwtService.extractClaim(jwt, "email", "access");

    // Load user details from database
    CustomUserDetails customUserDetails = customUserDetailsService.loadUserByUsername(email);

    try {
      // Validate token signature, expiration, and revocation status
      if (!jwtService.isTokenValid(jwt, customUserDetails, "access") || jwtService.isTokenRevoked(jwt, "access")) {
        sendAuthError(response, "Token validation failed");
      }
    } catch (JwtException e) {
      // Handle specific JWT errors (expired, malformed, etc)
      sendAuthError(response, e.getMessage());
    }

    // Set authentication in security context
    _setAuthenticationInContext(customUserDetails, request);
  }

  /**
   * Establishes the authenticated user in Spring Security's context
   */
  private void _setAuthenticationInContext(CustomUserDetails customUserDetails, HttpServletRequest request) {
    // Create authentication token (credentials nulled for security)
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        customUserDetails, // Principal
        null, // Cleared credentials
        customUserDetails.getAuthorities());

    // Add request details (IP, session ID, etc)
    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    // Store in thread-local SecurityContext
    SecurityContextHolder.getContext().setAuthentication(authToken);
  }

  /**
   * Standardized error response for authentication failures
   */
  private void sendAuthError(HttpServletResponse response, String message) throws IOException {
    // Clear any existing authentication
    SecurityContextHolder.clearContext();

    // Set response headers
    response.setContentType("application/json");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");

    // Write JSON error body
    response.getWriter().write(
        String.format("{\"status\": 401,\"error\": \"Unauthorized\", \"message\": \"%s\"}", message));

    // Ensure error is committed
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid/expired token");
  }

}
