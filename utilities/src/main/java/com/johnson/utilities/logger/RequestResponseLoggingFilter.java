package com.johnson.utilities.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import java.util.Map;

/**
 * Servlet Filter to log details of incoming requests and outgoing responses.
 * Captures User-Agent, IP Address, Path, Method, Timestamps, Body, Parameters
 * for requests.
 * Captures Current User (if authenticated), Response Message (body), Status
 * Code, Timestamp for responses.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100) // Ensures this filter runs after Spring Security's FilterChainProxy (default
                                         // order +50)
public class RequestResponseLoggingFilter implements Filter {

  private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

  // Configure the maximum length of the request/response body to log.
  // Large bodies can flood logs and consume memory.
  private static final int MAX_PAYLOAD_LENGTH = 2000; // characters

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    // Only process HTTP requests and responses
    if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
      chain.doFilter(request, response);
      return;
    }

    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    HttpServletResponse httpServletResponse = (HttpServletResponse) response;

    // Wrap the request and response to cache the content, allowing us to read the
    // body later
    ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpServletRequest);
    ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpServletResponse);

    long startTime = System.currentTimeMillis();
    Instant requestTimestamp = Instant.now();

    try {
      // Continue the filter chain with the wrapped request and response.
      // Controllers and other filters will process these wrapped objects.
      chain.doFilter(wrappedRequest, wrappedResponse);
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      Instant responseTimestamp = Instant.now();

      // --- Log Request Details ---
      logRequestDetails(wrappedRequest, duration, requestTimestamp);

      // --- Log Response Details ---
      logResponseDetails(wrappedRequest, wrappedResponse, responseTimestamp);

      // IMPORTANT: Copy the cached response content back to the original response
      // stream
      // This is necessary for the client to receive the response body.
      wrappedResponse.copyBodyToResponse();
    }
  }

  /**
   * Logs the details of the incoming request.
   */
  private void logRequestDetails(ContentCachingRequestWrapper request, long duration, Instant timestamp) {
    StringBuilder requestLog = new StringBuilder();
    requestLog.append(">>> Request: ");

    // Method and Path
    requestLog.append(request.getMethod()).append(" ").append(request.getRequestURI());

    // Query Parameters
    String queryString = request.getQueryString();
    if (queryString != null && !queryString.isEmpty()) {
      requestLog.append("?").append(queryString);
    }

    // Duration (from request start to response completion)
    requestLog.append(" (").append(duration).append(" ms)");

    logger.info(requestLog.toString());

    // Other Request Details
    logger.info("    Timestamp: {}", timestamp);
    logger.info("    IP Address: {}", getClientIpAddress(request));
    logger.info("    User-Agent: {}", request.getHeader("User-Agent"));

    // Request Body (Payload) - Available because of ContentCachingRequestWrapper
    String requestBody = getContentAsString(request.getContentAsByteArray(), request.getCharacterEncoding());
    logger.info("    Request Body: {}", formatPayload(requestBody));

    // Request Parameters (from form data or query string, useful if not logging
    // query string directly)
    // Note: getParameterMap() can be called multiple times on
    // ContentCachingRequestWrapper
    logRequestParameters(request);
  }

  /**
   * Logs the details of the outgoing response.
   */
  private void logResponseDetails(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
      Instant timestamp) {
    StringBuilder responseLog = new StringBuilder();
    responseLog.append("<<< Response: ");

    // Status Code
    responseLog.append("Status ").append(response.getStatus());

    // Current User (requires filter to run after Spring Security)
    String currentUser = "guest";
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      // Check if authentication exists, is authenticated, and is not the anonymous
      // user
      if (authentication != null && authentication.isAuthenticated()
          && !(authentication instanceof AnonymousAuthenticationToken)) {
        // Get the principal's name (usually username or email)
        currentUser = authentication.getName();
        // If your principal is a custom UserDetails object with a specific userId
        // field,
        // you might cast and access it like:
        // if (authentication.getPrincipal() instanceof YourCustomUserDetails) {
        // currentUser = ((YourCustomUserDetails)
        // authentication.getPrincipal()).getUserId();
        // }
      }
    } catch (Exception e) {
      // Catch potential exceptions if SecurityContextHolder is not fully initialized
      // or if there are issues accessing the principal.
      logger.debug("Could not determine current user for logging: {}", e.getMessage());
      currentUser = "N/A (Error retrieving user)";
    }
    responseLog.append(" | User: ").append(currentUser);

    logger.info(responseLog.toString());
    logger.info("    Timestamp: {}", timestamp);

    // Response Body (Payload) - Available because of ContentCachingResponseWrapper
    String responseBody = getContentAsString(response.getContentAsByteArray(), response.getCharacterEncoding());
    logger.info("    Response Body (Captured): {}", formatPayload(responseBody));

    // Response Headers (Optional, can be verbose)
    // logResponseHeaders(response);
  }

  /**
   * Helper method to extract content from a byte array, handling character
   * encoding.
   */
  private String getContentAsString(byte[] buf, String characterEncoding) {
    if (buf == null || buf.length == 0) {
      return "";
    }
    try {
      // Use provided character encoding or default to UTF-8
      return new String(buf, 0, buf.length,
          characterEncoding != null ? characterEncoding : StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException e) {
      // Log an error if encoding fails
      logger.error("Failed to get content as string with encoding {}", characterEncoding, e);
      return "[UNREADABLE BODY: " + e.getMessage() + "]";
    }
  }

  /**
   * Helper to format payload for logging (trimming and truncating).
   */
  private String formatPayload(String payload) {
    if (payload == null || payload.isEmpty()) {
      return "[empty]";
    }
    String trimmedPayload = payload.trim();
    if (trimmedPayload.length() > MAX_PAYLOAD_LENGTH) {
      return trimmedPayload.substring(0, MAX_PAYLOAD_LENGTH) + "... [TRUNCATED]";
    }
    return trimmedPayload;
  }

  /**
   * Helper to get the client IP address, considering standard headers like
   * X-Forwarded-For.
   */
  private String getClientIpAddress(HttpServletRequest request) {
    // Check for standard headers used by proxies/load balancers
    String xForwardedForHeader = request.getHeader("X-Forwarded-For");
    if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
      // In case of multiple proxies, the first IP is typically the client's IP
      return xForwardedForHeader.split(",")[0].trim();
    }
    // Fallback to the direct remote address if no proxy headers are present
    return request.getRemoteAddr();
  }

  /**
   * Optional helper to log request headers. Uncomment if needed.
   */
  // private void logRequestHeaders(HttpServletRequest request) {
  // Enumeration<String> headerNames = request.getHeaderNames();
  // if (headerNames != null) {
  // logger.info(" Request Headers:");
  // while (headerNames.hasMoreElements()) {
  // String headerName = headerNames.nextElement();
  // String headerValue = request.getHeader(headerName);
  // logger.info(" {}: {}", headerName, headerValue);
  // }
  // }
  // }

  /**
   * Helper to log request parameters (from query string or form data).
   */
  private void logRequestParameters(HttpServletRequest request) {
    Map<String, String[]> parameterMap = request.getParameterMap();
    if (parameterMap != null && !parameterMap.isEmpty()) {
      logger.info("    Request Parameters:");
      parameterMap.forEach((name, values) -> {
        logger.info("        {}: {}", name, String.join(",", values));
      });
    }
  }

  /**
   * Optional helper to log response headers. Uncomment if needed.
   */
  // private void logResponseHeaders(ContentCachingResponseWrapper response) {
  // logger.info(" Response Headers:");
  // for (String headerName : response.getHeaderNames()) {
  // for (String headerValue : response.getHeaders(headerName)) {
  // logger.info(" {}: {}", headerName, headerValue);
  // }
  // }
  // }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Initialization logic if needed
  }

  @Override
  public void destroy() {
    // Cleanup logic if needed
  }
}