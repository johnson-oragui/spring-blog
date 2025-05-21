package com.johnson.database.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.johnson.database.model.UserSessionModel;

public interface UserSessionRepository extends JpaRepository<UserSessionModel, String> {
  @Query(value = "SELECT * FROM blog_user_sessions u WHERE u.jti = :jti AND u.user_id = :userId AND u.device_id = :deviceId", nativeQuery = true)
  Optional<UserSessionModel> findUserSession(@Param("jti") String jti, @Param("userId") String userId,
      @Param("deviceId") String deviceId);

  @Query(value = "SELECT * FROM blog_user_sessions u WHERE u.device_id = :deviceId", nativeQuery = true)
  Optional<UserSessionModel> findUserSessionByDeviceId(@Param("deviceId") String deviceId);

  @Modifying
  @Query("UPDATE UserSessionModel u SET u.jti = :jti, u.updatedAt = CURRENT_TIMESTAMP WHERE u.userId = :userId AND u.deviceId = deviceId")
  int updateJti(@Param("jti") String jti, @Param("userId") String userId, @Param("deviceId") String deviceId);

  @Modifying
  @Query("UPDATE UserSessionModel u SET u.jti = :jti, u.isLoggedOut = :isLoggedOut, u.updatedAt = CURRENT_TIMESTAMP WHERE u.userId = :userId AND u.deviceId = deviceId")
  int updateJtiAndIsLoggedOut(@Param("jti") String jti, @Param("userId") String userId,
      @Param("deviceId") String deviceId, @Param("isLoggedOut") boolean isLoggedOut);

  @Modifying
  @Query("UPDATE UserSessionModel u SET u.isLoggedOut = TRUE, u.updatedAt = CURRENT_TIMESTAMP WHERE u.userId = :userId AND u.deviceId = :deviceId")
  int updateIsLoggedOut(@Param("userid") String userId, @Param("deviceId") String deviceId);
}
