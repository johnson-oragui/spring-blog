package com.johnson.blog.filters;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Supplier;

import org.springframework.security.core.GrantedAuthority;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.johnson.database.model.UserModel;

/// create a custom UserDetails so that i can include fields absent in userDetails,
/// so that making extra runs to the database for these user fields would be avoided.
/// and to avoid unneccessary loading of all user fields, a lazy loading is used
public class CustomUserDetails implements UserDetails {
  /// Cached after first load
  /// Subsequent calls to getFirstname(), etc. won't hit DB
  private UserModel user;
  private final Supplier<UserModel> userLoader;

  public CustomUserDetails(Supplier<UserModel> userLoader) {
    this.userLoader = userLoader;
  }

  // thread-safe
  private synchronized UserModel _getUser() {
    if (user == null) {
      user = userLoader.get();
    }
    return user;
  }

  // Standard UserDetails methods
  @Override
  public String getUsername() {
    return _getUser().getEmail();
  }

  @Override
  public String getPassword() {
    return _getUser().getPassword();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // could add user roles here when feature is introduced like so...
    // Convert your UserModel roles to Spring's GrantedAuthority
    // return user.getRoles().stream()
    // .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
    // .toList();
    return Collections.emptyList(); // example: ADMIN, USER
  }

  @Override
  public boolean isAccountNonExpired() {
    return true; // could Add account expiration logic if needed
  }

  @Override
  public boolean isAccountNonLocked() {
    return true; // could Add lock status logic
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true; // could Add password expiration logic
  }

  @Override
  public boolean isEnabled() {
    return true; // could Add activation status logic
  }

  // Custom field accessors
  public String getUserId() {
    return _getUser().getId();
  }

  public String getFirstname() {
    return _getUser().getFirstname();
  }

  // public String getIsDeleted() {
  // return _getUser().getIsDeleted();
  // }

  // public String getIsDeactivated() {
  // return _getUser().getIsDeactivated();
  // }

  public OffsetDateTime getCreatedAt() {
    return _getUser().getCreatedAt();
  }

  public OffsetDateTime getUpdatedAt() {
    return _getUser().getUpdatedAt();
  }

  /// When there is a change in user data, this method can be called.
  /// then the call for userDetails would fetch the updated user data.
  public void refreshUser() {
    this.user = null;
  }

  public UserModel getUser() {
    return user;
  }

}
