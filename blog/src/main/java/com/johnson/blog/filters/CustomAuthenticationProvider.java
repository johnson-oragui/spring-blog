package com.johnson.blog.filters;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.johnson.blog.service.CustomUserDetailsService;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
  private final CustomUserDetailsService customUserDetailsService;
  private final PasswordEncoder passwordEncoder;

  public CustomAuthenticationProvider(CustomUserDetailsService customUserDetailsService,
      PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
    this.customUserDetailsService = customUserDetailsService;
  }

  /// When the default authenticationManager.authenticate is called for login, the
  /// manager would
  /// locate this CustomAuthenticationProvider and use this authenticate method,
  /// passing in the
  /// UsernamePasswordAuthenticationToken(email, password). the
  /// UsernamePasswordAuthenticationToken
  /// injects Authentication object to this autheticate in which this .autheticate
  /// uses to authorize
  /// the user attempting a login
  @Override
  public Authentication authenticate(Authentication auth) throws AuthenticationException {
    String email = auth.getName();
    String password = auth.getCredentials().toString();

    /// the customUserDetailsService.loadUserByUsername uses the passed
    /// email/username(depending)
    /// to fetch the user from the database using the UserRepository injected into
    /// customUserDetailsService. customUserDetailsService uses loadUserByUsername
    /// method
    /// to fetch the User object(throws user not found if user does not exist),
    /// and then creates a UserDetails object whic takes email/username,
    /// password(hashed, gotten from database), roles.
    /// this UserDetails object is returned from customUserDetailsService.
    CustomUserDetails customUserDetails = this.customUserDetailsService.loadUserByUsername(email);

    /// the passwordEncoder now hashed the plain password gotten from the intended
    /// authentication payloads
    /// and tries to match with the hashed pasword set in the UserDetails
    if (passwordEncoder.matches(password, customUserDetails.getPassword())) {
      /// if there is a match, then the userDetails, and roles are set
      /// in UsernamePasswordAuthenticationToken, then
      /// UsernamePasswordAuthenticationToken in return
      /// transports the username/email and password and roles to the authentication
      /// system. the password is set to null to tell the authentication system that
      /// authentication with credentials were cleared, and that password serves no
      /// other purpose. unlike prior to authentication, the password is set to null.
      return new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
    }

    throw new BadCredentialsException("Invalid Credentials");
  }

  /// This overriden method tells the AuthenticationProviderManager that this
  /// CustomAuthenticationProvider supports usernamePasswordAuthntication and
  /// should use it.
  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }
}
