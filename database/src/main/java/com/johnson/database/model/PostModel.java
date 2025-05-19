package com.johnson.database.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "blog_posts")
public class PostModel extends BaseModel {

  @Column(name = "title", nullable = false, length = 100)
  private String title;

  @Column(name = "content", length = 1500, nullable = false)
  private String content;

  @Column(name = "c", nullable = true)
  private LocalDate c;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "thumbnail", nullable = true)
  private String thumbnail;

  @Column(name = "genre", nullable = false)
  private String genre;

  @ManyToOne(targetEntity = UserModel.class, cascade = CascadeType.ALL)
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel blogger;
}
