package com.johnson.utilities.dtos.authDtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserRegistrationDto {
  @NotBlank(message = "Firstname is required")
  @Size(min = 3, max = 100, message = "firstname must have lenght up to 3 and not more than 100")
  @Pattern(regexp = "^*[a-zA-Z]*$", message = "firstname must not include special characters")
  private String firstname;

  @Email(message = "Email should be valid")
  @NotBlank(message = "Email is required")
  private String email;

  @NotBlank(message = "Password is required")
  @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[*!@#%&_\\-+=]).{8,}$", message = "Password must contain at least one digit, one lowercase, one uppercase letter, one special character, and be at least 8 characters long")
  private String password;

  @NotBlank(message = "confirmPassword is required")
  private String confirmPassword;

  public UserRegistrationDto() {
  }

  public UserRegistrationDto(
      String firstname, String email, String password, String confirmPassword) {
    this.firstname = firstname;
    this.email = email;
    this.password = password;
    this.confirmPassword = confirmPassword;
  }

  // Getters and setters
  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

  public String getConfirmPassword() {
    return this.confirmPassword;
  }
}
