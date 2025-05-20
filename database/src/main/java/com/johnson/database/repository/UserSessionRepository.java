package com.johnson.database.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.johnson.database.model.UserSessionModel;

public interface UserSessionRepository extends JpaRepository<UserSessionModel, String> {
  @Query(value = "SELECT * FROM UserSessionModel u WHERE u.jti = :jti AND u.userId = :userId AND u.deviceId = :deviceId", nativeQuery = true)
  Optional<UserSessionModel> findUserSession(@Param("jti") String jti, @Param("userId") String userId,
      @Param("deviceId") String deviceId);

  @Modifying
  @Query("UPDATE UserSessionModel u SET u.jti = :jti WHERE u.userId = :userId AND u.deviceId = deviceId")
  int updateJti(@Param("jti") String jti, @Param("userId") String userId, @Param("deviceId") String deviceId);

  @Modifying
  @Query("UPDATE UserSessionModel u SET u.isLoggedOut = TRUE WHERE u.userId = :userId AND u.deviceId = :deviceId")
  int updateIsLoggedOut(@Param("userid") String userId, @Param("device_id") String deviceId);
}
