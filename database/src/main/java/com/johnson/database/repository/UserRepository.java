package com.johnson.database.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.johnson.database.model.UserModel;

import jakarta.persistence.Id;

public interface UserRepository extends JpaRepository<UserModel, String> {

  Optional<UserModel> findByEmail(String email);

  Optional<UserModel> findById(Id id);

  @Modifying
  @Query("UPDATE user SET email = :email WHERE id = :id")
  int updateUser(@Param("id") Id id, @Param("email") String email);

  @Query(value = "SELECT * FROM blog_users WHERE created_at > :date", nativeQuery = true)
  List<UserModel> findUsersCreatedAfter(@Param("date") LocalDateTime date);

}
