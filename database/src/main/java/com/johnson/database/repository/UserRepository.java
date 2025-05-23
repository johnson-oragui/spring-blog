package com.johnson.database.repository;

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

  boolean existByEmail(String email);

  Optional<UserModel> findById(Id id);

  @Modifying
  @Query("UPDATE UserModel u SET u.email = :email, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :id")
  int updateUser(@Param("id") Id id, @Param("email") String email);

  @Query(value = "SELECT COUNT(*) FROM users;", nativeQuery = true)
  long countUsers();

  @Query(value = "SELECT * FROM users LIMIT :limit OFFSET :offset", nativeQuery = true)
  List<UserModel> getUsers(@Param("limit") int limit, @Param("offset") int offset);

}
