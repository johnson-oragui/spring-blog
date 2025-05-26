package com.johnson.blog.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.johnson.blog.service.AuthenticationService;
import com.johnson.utilities.dtos.BaseApiResponse;
import com.johnson.utilities.dtos.authDtos.UserLoginResponseDto;
import com.johnson.utilities.dtos.authDtos.UserDataResponseDto;
import com.johnson.utilities.dtos.authDtos.UserLoginDto;
import com.johnson.utilities.dtos.authDtos.UserRegistrationDto;

import jakarta.validation.Valid;
import lombok.NonNull;

@RestController
@Validated
@RequestMapping("api/v1/auth")
public class AuthController {
  @NonNull
  private final AuthenticationService authenticationService;

  public AuthController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @PostMapping("register")
  public ResponseEntity<BaseApiResponse<UserDataResponseDto>> createUser(
      @Valid @RequestBody UserRegistrationDto incomingUserData) {
    return authenticationService.register(incomingUserData);
  }

  @PostMapping("login")
  public ResponseEntity<BaseApiResponse<UserLoginResponseDto>> authenticateUser(
      @Valid @RequestBody UserLoginDto userLoginDto, HttpServletRequest request, HttpServletResponse response) {
    return authenticationService.authenticate(userLoginDto, request, response);
  }

  @PostMapping("refresh")
  public ResponseEntity<BaseApiResponse<Map<String, Object>>> refreshToken(HttpServletRequest request,
      HttpServletResponse response) {
    return authenticationService.refreshToken(request, response);
  }

  @PostMapping("logout")
  public ResponseEntity<BaseApiResponse<Map<String, Object>>> logoutUser(HttpServletRequest request) {

    return authenticationService.logoutUser(request);
  }

}
