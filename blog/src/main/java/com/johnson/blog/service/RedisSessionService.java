package com.johnson.blog.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.johnson.database.repository.UserSessionRepository;

public class RedisSessionService {
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;
  private final UserSessionRepository userSessionRepository;

  public RedisSessionService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper,
      UserSessionRepository userSessionRepository) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.userSessionRepository = userSessionRepository;
  }

  private String _getSessionKey(String userId, String deviceId) {
    return "session:" + userId + ":" + deviceId;
  }

  public void saveSession(String userId, String deviceId, String ipAddress, String location, String jti) {
    Map<String, Object> sessionData = Map.of(
        "ipAddress", ipAddress,
        "location", location,
        "isLoggedOut", false,
        "jti", jti,
        "createdAt", Instant.now().toString());

    redisTemplate.opsForHash().putAll(_getSessionKey(userId, deviceId), sessionData);
  }

  public boolean isLoggedOut(String userId, String deviceId) {
    Object status = redisTemplate.opsForHash().get(_getSessionKey(userId, deviceId), deviceId);
    return Boolean.TRUE.equals(status);
  }

  public void logoutAllDevices(String userId) {
    Set<String> keys = redisTemplate.keys("session:" + userId + ":*");
    if (keys != null) {
      for (String key : keys) {
        redisTemplate.opsForHash().delete(key);
        userSessionRepository.logoutAllSessionsByUserId(userId);
      }
    }
  }

  public void logoutADevice(String userId, String deviceId) {
    String key = _getSessionKey(userId, deviceId);
    redisTemplate.opsForHash().delete(key);
    userSessionRepository.logoutASession(userId, deviceId);
  }

  public List<Map<String, Object>> getAllSessions(String userId) {
    Set<String> keys = redisTemplate.keys("session:" + userId + ":*");
    List<Map<String, Object>> sessions = new ArrayList<>();

    if (keys != null) {
      for (String key : keys) {
        Map<Object, Object> data = redisTemplate.opsForHash().entries(key);
        sessions
            .add(data.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue)));
      }
    }

    return sessions;
  }
}
