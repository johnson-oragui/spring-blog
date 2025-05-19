package com.johnson.utilities.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {
  private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

  public static String hashPassword(String plainPassword) {
    return encoder.encode(plainPassword);
  }

  public static boolean verifyPassword(String plainPassword, String hashedPassword) {
    return encoder.matches(plainPassword, hashedPassword);
  }
}
