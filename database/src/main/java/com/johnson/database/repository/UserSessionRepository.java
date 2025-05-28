package com.johnson.database.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.johnson.database.model.UserSessionModel;

import jakarta.transaction.Transactional;

public interface UserSessionRepository extends JpaRepository<UserSessionModel, String> {
  @Query(value = "SELECT * FROM blog_user_sessions u WHERE u.jti = :jti AND u.user_id = :userId AND u.device_id = :deviceId", nativeQuery = true)
  Optional<UserSessionModel> findUserSession(@Param("jti") String jti, @Param("userId") String userId,
      @Param("deviceId") String deviceId);

  @Query(value = "SELECT * FROM blog_user_sessions u WHERE u.device_id = :deviceId", nativeQuery = true)
  Optional<UserSessionModel> findUserSessionByDeviceId(@Param("deviceId") String deviceId);

  @Modifying
  @Query(value = "UPDATE blog_user_sessions u SET u.jti = :jti, u.updated_at = CURRENT_TIMESTAMP WHERE u.user_id = :userId AND u.device_id = :deviceId", nativeQuery = true)
  @Transactional
  int updateJti(@Param("jti") String jti, @Param("userId") String userId, @Param("deviceId") String deviceId);

  @Modifying
  @Query(value = "UPDATE blog_user_sessions u SET u.jti = :jti, u.is_logged_out = :isLoggedOut, u.updated_at = CURRENT_TIMESTAMP WHERE u.user_id = :userId AND u.deviceId = deviceId", nativeQuery = true)
  @Transactional
  int updateJtiAndIsLoggedOut(@Param("jti") String jti, @Param("userId") String userId,
      @Param("deviceId") String deviceId, @Param("isLoggedOut") boolean isLoggedOut);

  @Modifying
  @Query(value = "UPDATE blog_user_sessions u SET u.is_logged_out = TRUE, u.updated_at = CURRENT_TIMESTAMP WHERE u.user_id = :userId AND u.deviceId = :deviceId", nativeQuery = true)
  @Transactional
  int logoutASession(@Param("userId") String userId, @Param("deviceId") String deviceId);

  @Modifying
  @Query(value = "UPDATE blog_user_sessions u SET u.is_logged_out = TRUE, u.updated_at = CURRENT_TIMESTAMP WHERE u.user_id = :userId", nativeQuery = true)
  @Transactional
  int logoutAllSessionsByUserId(@Param("userid") String userId);
}
