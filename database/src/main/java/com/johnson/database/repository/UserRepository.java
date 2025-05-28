package com.johnson.database.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.johnson.database.model.UserModel;

import jakarta.transaction.Transactional;

public interface UserRepository extends JpaRepository<UserModel, String> {

  Optional<UserModel> findByEmail(String email);

  // @Query("SELECT COUNT(u) > 0 FROM UserModel u WHERE u.email = :email")
  boolean existsByEmail(@Param("email") String email);

  Optional<UserModel> findById(String id);

  @Transactional
  @Modifying
  @Query(value = "UPDATE blog_users u SET u.email = :email, u.updated_at = CURRENT_TIMESTAMP WHERE u.id = :id", nativeQuery = true)
  int updateUser(@Param("id") String id, @Param("email") String email);

  @Query(value = "SELECT COUNT(*) FROM users;", nativeQuery = true)
  long countUsers();

  @Query(value = "SELECT * FROM users LIMIT :limit OFFSET :offset", nativeQuery = true)
  List<UserModel> getUsers(@Param("limit") int limit, @Param("offset") int offset);

}
