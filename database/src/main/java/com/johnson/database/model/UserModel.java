package com.johnson.database.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "blog_users", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "email" }, name = "uq_blog_users_email") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserModel extends BaseModel {

  @Column(name = "firstname", nullable = false, length = 100)
  private String firstname;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "email", nullable = false, length = 100, unique = true)
  private String email;

  @OneToMany(mappedBy = "blogger", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PostModel> posts = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UserSessionModel> sessions = new ArrayList<>();
}
