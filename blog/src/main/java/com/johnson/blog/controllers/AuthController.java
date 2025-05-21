package com.johnson.blog.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.johnson.database.service.UserService;
import com.johnson.utilities.dtos.BaseApiResponse;
import com.johnson.utilities.dtos.authDtos.UserLoginResponseDto;
import com.johnson.utilities.dtos.authDtos.UserDataResponseDto;
import com.johnson.utilities.dtos.authDtos.UserLoginDto;
import com.johnson.utilities.dtos.authDtos.UserRegistrationDto;

import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("api/v1/auth")
public class AuthController {
  @Autowired
  private UserService userService;

  @PostMapping("register")
  public ResponseEntity<BaseApiResponse<UserDataResponseDto>> createUser(
      @Valid @RequestBody UserRegistrationDto incomingUserData) {
    return userService.createUser(incomingUserData);
  }

  @PostMapping("login")
  public ResponseEntity<BaseApiResponse<UserLoginResponseDto>> authenticateUser(
      @Valid @RequestBody UserLoginDto userLoginDto, HttpServletResponse response) {
    return userService.authenticate(userLoginDto, response);
  }

  @GetMapping("refresh")
  public ResponseEntity<BaseApiResponse<Map<String, Object>>> refreshToken(HttpServletRequest request,
      HttpServletResponse response) {
    return userService.refreshToken(request, response);
  }

}
