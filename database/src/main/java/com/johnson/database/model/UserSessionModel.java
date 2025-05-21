package com.johnson.database.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "blog_user_sessions", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "device_id", "jti" }, name = "composite_uq_blog_user_sessions_device_id_jti"),
    @UniqueConstraint(columnNames = { "id" }, name = "uq_blog_user_sessions_id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSessionModel extends BaseModel {
  @Column(name = "device_id", nullable = false, length = 100)
  private String deviceId;

  @Column(name = "jti", nullable = false, length = 60)
  private String jti;

  @Column(name = "is_logged_out", nullable = false)
  private boolean isLoggedOut;

  protected void setIsLoggedOutOnCreate() {
    isLoggedOut = false;
  }

  @Column(name = "user_id", insertable = false, updatable = false)
  private String userId;

  @ManyToOne(targetEntity = UserModel.class, cascade = CascadeType.REMOVE)
  @JoinColumn(name = "user_id", nullable = false)
  private UserModel user; // userSession.getUser().getId()
}
