package com.johnson.blog.service;

import java.util.function.Supplier;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.johnson.blog.filters.CustomUserDetails;
import com.johnson.database.model.UserModel;
import com.johnson.database.repository.UserRepository;

/// UserDetailsService/CustomUserDetailsService serves as Spring Security's bridge between
/// the database UserRepository and its authentication system.
/// transalates UserModel to UserDetails(spring security interface).
/// decouples database access from security logic.
/// uses UsernameNotFoundException to trigger authetication failuer when user does not exist or the user has no GrantedAuthority.
/// enables role-based authorization.
/// this CustomUserDetailsService (UserDetailsService) is used by the AUthenticationManager which uses AuthenticationProvider behind the scenes
/// to fetch the userDetails just like in this case i override the AuthenticationProvider (CustomAuthenticationProvider) and used the
/// CustomUserDetailsService to fetch UserDetails and processed authentiation
@Component
public class CustomUserDetailsService implements UserDetailsService {

  private UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository, OpenAPI apiInfo) {
    this.userRepository = userRepository;
  }

  // could return the UserDetails or the CustomUserDetails
  @Override
  public CustomUserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    /// Create a Supplier that fetches user when needed.
    /// Database call only happens when first accessing user data.
    /// Useful when only username/password are needed for auth
    Supplier<UserModel> userLoader = () -> userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found"));

    return new CustomUserDetails(userLoader);
  }

}
