package com.johnson.database.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.johnson.database.model.UserModel;
import com.johnson.database.model.UserSessionModel;
import com.johnson.database.repository.UserRepository;
import com.johnson.database.repository.UserSessionRepository;
import com.johnson.utilities.UUIDGenerator;
import com.johnson.utilities.config.ConfigUtils;
import com.johnson.utilities.dtos.BaseApiResponse;
import com.johnson.utilities.dtos.PaginationMeta;
import com.johnson.utilities.dtos.authDtos.UserLoginResponseDto;
import com.johnson.utilities.dtos.authDtos.UserDataResponseDto;
import com.johnson.utilities.dtos.authDtos.UserLoginDto;
import com.johnson.utilities.dtos.authDtos.UserRegistrationDto;
import com.johnson.utilities.exceptions.ConflictException;
import com.johnson.utilities.exceptions.InternalServerException;
import com.johnson.utilities.exceptions.UnauthorizedException;
import com.johnson.utilities.exceptions.ValidationException;
import com.johnson.utilities.exceptions.BadRequestException;
import com.johnson.utilities.security.JwtUtil;
import com.johnson.utilities.security.PasswordUtil;

// import jakarta.xml.bind.ValidationException;

@Service
@Transactional
public class UserService {
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private UserSessionRepository userSessionRepository;

  @Transactional(readOnly = true)
  public Optional<UserModel> getUserByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public ResponseEntity<BaseApiResponse<UserDataResponseDto>> createUser(UserRegistrationDto user) {
    if (!user.getConfirmPassword().equals(user.getPassword())) {
      throw new ValidationException("password and confirm password must match");
    }
    Optional<UserModel> emailTaken = userRepository.findByEmail(user.getEmail());
    if (emailTaken.isPresent()) {

      throw new ConflictException("Email already in use");
    }
    UserModel newUser = new UserModel();
    newUser.setPassword(PasswordUtil.hashPassword(user.getPassword()));
    newUser.setEmail(user.getEmail());
    newUser.setFirstname(user.getFirstname());
    UserModel saved = userRepository.save(newUser);
    UserDataResponseDto userDataResponseDto = new UserDataResponseDto(
        saved.getId(),
        saved.getFirstname(),
        saved.getEmail(),
        saved.getCreatedAt(),
        saved.getUpdatedAt());

    BaseApiResponse<UserDataResponseDto> response = BaseApiResponse.success("User registered successfully",
        userDataResponseDto);

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  public ResponseEntity<BaseApiResponse<UserLoginResponseDto>> authenticate(UserLoginDto userLoginDto) {
    Optional<UserModel> userExists = userRepository.findByEmail(userLoginDto.getEmail());
    if (!userExists.isPresent()) {
      throw new UnauthorizedException("Invalid Credentials");
    }
    UserModel user = userExists.get();

    boolean isValidPassword = PasswordUtil.verifyPassword(userLoginDto.getPassword(), user.getPassword());
    if (!isValidPassword) {
      throw new UnauthorizedException("Invalid Credentials");
    }
    String jti = UUIDGenerator.generateUUIDv7();
    String deviceId = userLoginDto.getDeviceId();

    String token = JwtUtil.generateAccessToken(user.getId(), user.getEmail(), jti, deviceId);
    // add user sessions here
    Optional<UserSessionModel> deviceIdExists = userSessionRepository
        .findUserSessionByDeviceId(deviceId);
    if (deviceIdExists.isPresent()) {
      if (!user.getId().equals(deviceIdExists.get().getUser().getId())) {
        throw new BadRequestException("device id must be unique");
      }

      // logging in deviceid is same with existing device
      // update the jti with the newly generated jti and update is_logged_out to false
      int updated = userSessionRepository.updateJtiAndIsLoggedOut(jti, user.getId(), userLoginDto.getDeviceId(), false);
      if (updated < 1) {
        throw new InternalServerException("Something went wrong");
      }

    }
    if (!deviceIdExists.isPresent()) {
      UserSessionModel userSessionModel = new UserSessionModel();
      userSessionModel.setJti(jti);
      userSessionModel.setDeviceId(deviceId);
      userSessionModel.setUser(user);
      userSessionRepository.save(userSessionModel);
    }

    Map<String, Object> accessToken = new HashMap<>();
    accessToken.put("token", token);
    accessToken.put("expireAt", ConfigUtils.JWT_EXPIRATION * 1000);
    UserDataResponseDto userDataResponseDto = new UserDataResponseDto(
        user.getId(),
        user.getFirstname(),
        user.getEmail(),
        user.getCreatedAt(),
        user.getUpdatedAt());
    UserLoginResponseDto userLoginResponseDto = new UserLoginResponseDto(
        accessToken,
        userDataResponseDto);

    BaseApiResponse<UserLoginResponseDto> response = BaseApiResponse.success("Login success", userLoginResponseDto);

    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  // @Transactional(rollbackFor = { ValidationException.class,
  // DataIntegrityViolationException.class })
  // public Optional<UserModel> updateUser(String email, User userDetails) {
  // Optional<UserModel> user = getUserByEmail(email);
  // Update fields
  // if (user.isPresent()) {
  // return userRepository.save(user.get());
  // }
  // return Optional.empty();
  // }

  @GetMapping("/users")
  public ResponseEntity<BaseApiResponse<List<UserDataResponseDto>>> getUsers(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    List<UserModel> users = userRepository.getUsers(page, size);
    List<UserDataResponseDto> UserDataResponseDto = new ArrayList<>();
    for (UserModel user : users) {
      UserDataResponseDto.add(new UserDataResponseDto(user.getId(),
          user.getFirstname(), user.getEmail(), user.getCreatedAt(), user.getUpdatedAt()));
    }
    long totalItems = userRepository.countUsers();
    int totalPages = (int) Math.ceil((double) totalItems / size);

    PaginationMeta meta = new PaginationMeta(page, size, totalItems, totalPages);
    BaseApiResponse<List<UserDataResponseDto>> response = BaseApiResponse.successWithPagination(
        "Users fetched successfully",
        UserDataResponseDto, meta);

    return ResponseEntity.ok(response);
  }
}
