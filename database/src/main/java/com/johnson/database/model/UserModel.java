package com.johnson.database.model;

import java.util.ArrayList;
import java.util.List;

// import org.hibernate.annotations.processing.Pattern;

import jakarta.persistence.*;
// import javax.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "blog_users")
@Getter
@Setter
public class UserModel extends BaseModel {

  @Column(name = "firstname", nullable = false, length = 100)
  private String firstname;

  // @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$", message =
  // "Password must contain at least 1 digit, 1 lowercase, 1 uppercase letter, and
  // be at least 8 characters long")
  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "email", nullable = false, length = 100, unique = false)
  private String email;

  @OneToMany(mappedBy = "blog_users", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PostModel> posts = new ArrayList<>();
}
