package com.johnson.database.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.johnson.database.model.UserSession;

public interface UserSessionRepository extends JpaRepository<UserSession, String> {
  @Query(value = "SELECT * FROM UserSession WHERE jti = :jti AND user_id = :userId AND device_id = :deviceId", nativeQuery = true)
  Optional<UserSession> findUserSession(@Param("jti") String jti, @Param("userId") String userId,
      @Param("deviceId") String deviceId);

  @Modifying
  @Query("UPDATE UserSession SET jti = :jti WHERE user_id = :userId AND device_id = deviceId")
  int updateJti(@Param("jti") String jti, @Param("userId") String userId, @Param("deviceId") String deviceId);

  @Modifying
  @Query("UPDATE UserSession SET is_logged_out = TRUE WHERE user_id = :userId AND device_id = :deviceId")
  int updateIsLoggedOut(@Param("userid") String userId, @Param("device_id") String deviceId);
}
