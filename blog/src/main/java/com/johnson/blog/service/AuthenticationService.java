package com.johnson.blog.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.johnson.blog.filters.CustomUserDetails;
import com.johnson.database.model.UserModel;
import com.johnson.database.model.UserSessionModel;
import com.johnson.database.repository.UserRepository;
import com.johnson.database.repository.UserSessionRepository;
import com.johnson.utilities.UUIDGenerator;
import com.johnson.utilities.dtos.BaseApiResponse;
import com.johnson.utilities.dtos.authDtos.UserDataResponseDto;
import com.johnson.utilities.dtos.authDtos.UserLoginDto;
import com.johnson.utilities.dtos.authDtos.UserLoginResponseDto;
import com.johnson.utilities.dtos.authDtos.UserRegistrationDto;
import com.johnson.utilities.exceptions.BadRequestException;
import com.johnson.utilities.exceptions.ConflictException;
import com.johnson.utilities.exceptions.InternalServerException;
import com.johnson.utilities.exceptions.UnauthorizedException;
import com.johnson.utilities.exceptions.ValidationException;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class AuthenticationService {

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final JwtService jwtService;

  private final AuthenticationManager authenticationManager;

  private final UserSessionRepository userSessionRepository;

  private final GeoLocationService geoLocationService;

  private final RedisSessionService redisSessionService;

  private final CustomUserDetailsService customUserDetailsService;

  @Value("${application.security.jwt.expiration}")
  private long JWT_EXPIRATION; // in seconds

  public AuthenticationService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      AuthenticationManager authenticationManager,
      UserSessionRepository userSessionRepository,
      GeoLocationService geoLocationService,
      RedisSessionService redisSessionService,
      CustomUserDetailsService customUserDetailsService) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
    this.userRepository = userRepository;
    this.userSessionRepository = userSessionRepository;
    this.geoLocationService = geoLocationService;
    this.redisSessionService = redisSessionService;
    this.customUserDetailsService = customUserDetailsService;
  }

  @Transactional(rollbackOn = {
      InternalServerException.class,
      BadRequestException.class,
      ValidationException.class,
      DataIntegrityViolationException.class
  })
  public ResponseEntity<BaseApiResponse<UserDataResponseDto>> register(UserRegistrationDto userRegistrationDto) {
    if (!userRegistrationDto.getConfirmPassword().equals(userRegistrationDto.getPassword())) {
      throw new ValidationException("password and confirm password must match");
    }

    if (userRepository.existsByEmail(userRegistrationDto.getEmail())) {

      throw new ConflictException("Email already in use");
    }

    UserModel newUser = new UserModel();
    newUser.setPassword(passwordEncoder.encode(userRegistrationDto.getPassword()));
    newUser.setEmail(userRegistrationDto.getEmail());
    newUser.setFirstname(userRegistrationDto.getFirstname());
    UserModel saved = userRepository.save(newUser);
    UserDataResponseDto userDataResponseDto = new UserDataResponseDto(
        saved.getId(),
        saved.getFirstname(),
        saved.getEmail(),
        saved.getCreatedAt(),
        saved.getUpdatedAt());

    BaseApiResponse<UserDataResponseDto> response = BaseApiResponse.success(
        "User registered successfully",
        201,
        userDataResponseDto);

    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @Transactional(rollbackOn = {
      InternalServerException.class,
      BadRequestException.class,
      ValidationException.class,
      DataIntegrityViolationException.class
  })
  public ResponseEntity<BaseApiResponse<UserLoginResponseDto>> authenticate(UserLoginDto userLoginDto,
      HttpServletRequest request,
      HttpServletResponse response) {

    /// authenticationManager deletegates AutheticationProvider.
    /// Meaning that the default AuthenticationProviderManager now checks in with
    /// AuthenticationProvider
    /// and finds the CustomAuthenticationProvider (since it implements the
    /// supports(UsernamePasswordAuthenticationToken.class)).
    /// the UsernamePasswordAuthenticationToken is takes email and password cos
    /// authentication
    /// is still not cleared.
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            userLoginDto.getEmail(),
            userLoginDto.getPassword()));
    /// gets the customUserDetails (UserDetails with extra UserModel fields)
    CustomUserDetails customUserDetails = (CustomUserDetails) auth.getPrincipal();

    String jti = UUIDGenerator.generateUUIDv7();
    String location = geoLocationService.geoLocationFromIP(request.getRemoteAddr());

    Map<String, Object> extraClaims = this._buildClaims(
        customUserDetails.getUserId(),
        customUserDetails.getUsername(),
        userLoginDto.getDeviceId(),
        jti,
        "access",
        request.getHeader("User-Agent"),
        request.getRemoteAddr(),
        location);

    String jwtToken = jwtService.buildToken(extraClaims, customUserDetails, "access");
    extraClaims.put("tokenType", "refresh");
    String refreshJwtToken = jwtService.buildToken(extraClaims, customUserDetails, "refresh");

    Optional<UserSessionModel> deviceIdExists = userSessionRepository
        .findUserSessionByDeviceId(userLoginDto.getDeviceId());
    if (deviceIdExists.isPresent()) {
      if (!customUserDetails.getUserId().equals(deviceIdExists.get().getUser().getId())) {
        throw new BadRequestException("device id must be unique");
      }

      // logging in deviceid is same with existing device
      // update the jti with the newly generated jti and update is_logged_out to false
      int updated = userSessionRepository.updateJtiAndIsLoggedOut(jti, customUserDetails.getUserId(),
          userLoginDto.getDeviceId(),
          false);
      if (updated < 1) {
        throw new InternalServerException("Something went wrong");
      }

    }
    if (!deviceIdExists.isPresent()) {
      UserSessionModel userSessionModel = new UserSessionModel();
      userSessionModel.setJti(jti);
      userSessionModel.setDeviceId(userLoginDto.getDeviceId());
      userSessionModel.setIpAddress(request.getRemoteAddr());
      userSessionModel.setLocation(location);
      userSessionModel.setUser(customUserDetails.getUser());
      userSessionRepository.save(userSessionModel);
    }

    Map<String, Object> accessToken = new HashMap<>();
    accessToken.put("token", jwtToken);
    accessToken.put("expireAt", JWT_EXPIRATION * 1000L);
    UserDataResponseDto userDataResponseDto = new UserDataResponseDto(
        customUserDetails.getUserId(),
        customUserDetails.getFirstname(),
        customUserDetails.getUsername(),
        customUserDetails.getCreatedAt(),
        customUserDetails.getUpdatedAt());
    UserLoginResponseDto userLoginResponseDto = new UserLoginResponseDto(
        accessToken,
        userDataResponseDto);

    response.setHeader("X-Refresh-Token", refreshJwtToken);

    this.redisSessionService.saveSession(customUserDetails.getUserId(), userLoginDto.getDeviceId(),
        request.getRemoteAddr(),
        location, jti);

    BaseApiResponse<UserLoginResponseDto> baseApiResponse = BaseApiResponse.success(
        "Login success",
        200,
        userLoginResponseDto);
    return new ResponseEntity<>(baseApiResponse, HttpStatus.OK);
  }

  @Transactional(rollbackOn = {
      InternalServerException.class,
      BadRequestException.class,
      ValidationException.class,
      DataIntegrityViolationException.class
  })
  public ResponseEntity<BaseApiResponse<Map<String, Object>>> refreshToken(HttpServletRequest request,
      HttpServletResponse response) {
    String headerRefreshToken = request.getHeader("X-Refresh-Token");
    if (headerRefreshToken == null) {
      throw new UnauthorizedException("Refresh token missing");
    }

    try {
      String userId = jwtService.extractClaim(headerRefreshToken, "userId", "refresh");
      String email = jwtService.extractClaim(headerRefreshToken, "email", "refresh");
      String tokenType = jwtService.extractClaim(headerRefreshToken, "tokenType", "refresh");
      String deviceId = jwtService.extractClaim(headerRefreshToken, "deviceId", "refresh");
      String jti = jwtService.extractClaim(headerRefreshToken, "jti", "refresh");

      if (!tokenType.equals("refresh")) {
        throw new UnauthorizedException("Only Refresh Token allowed");
      }

      Optional<UserSessionModel> userSessionExists = userSessionRepository.findUserSession(jti, userId, deviceId);

      if (!userSessionExists.isPresent()) {
        throw new UnauthorizedException("Invalid session");
      }
      if (userSessionExists.get().isLoggedOut()) {
        throw new UnauthorizedException("Session has expired");
      }
      if (!userSessionExists.get().getJti().equals(jti)) {
        throw new UnauthorizedException("Invalid Refresh Token");
      }

      String newJti = UUIDGenerator.generateUUIDv7();

      long isUpdated = userSessionRepository.updateJti(newJti, userId, deviceId);

      if (isUpdated < 1) {
        throw new InternalServerException("An unexpected error occurred. Could not refresh token.");
      }

      String location = geoLocationService.geoLocationFromIP(request.getRemoteAddr());

      Map<String, Object> extraClaims = this._buildClaims(
          userId,
          email,
          deviceId,
          newJti,
          "access",
          request.getHeader("User-Agent"),
          request.getRemoteAddr(),
          location);

      CustomUserDetails customUserDetails = this.customUserDetailsService.loadUserByUsername(email);

      String jwtToken = jwtService.buildToken(extraClaims, customUserDetails, "access");

      extraClaims.put("tokenType", "refresh");
      String refreshJwtToken = jwtService.buildToken(extraClaims, customUserDetails, "refresh");

      response.setHeader("X-Refresh-Token", refreshJwtToken);

      Map<String, Object> newTokenMap = new HashMap<>();
      newTokenMap.put("accessToken", jwtToken);
      newTokenMap.put("expiresAt", JWT_EXPIRATION * 1000L);
      BaseApiResponse<Map<String, Object>> baseApiResponse = BaseApiResponse.success(
          "Tokens refreshed successfully",
          00,
          newTokenMap);

      ResponseEntity<BaseApiResponse<Map<String, Object>>> responseEntity = new ResponseEntity<>(baseApiResponse,
          HttpStatus.OK);
      userSessionRepository.flush();

      return responseEntity;
    } catch (JwtException e) {
      throw new UnauthorizedException(e.getMessage());
    }
  }

  @Transactional(rollbackOn = {
      InternalServerException.class,
      BadRequestException.class,
      ValidationException.class,
      DataIntegrityViolationException.class
  })
  public ResponseEntity<BaseApiResponse<Map<String, Object>>> logoutUser(HttpServletRequest request) {
    String jwtToken = request.getHeader("Authorization").substring(7);
    String userId = jwtService.extractClaim(jwtToken, "userId", "access");
    String deviceId = jwtService.extractClaim(jwtToken, "deviceId", "access");
    redisSessionService.logoutADevice(userId, deviceId);

    Map<String, Object> data = new HashMap<>();
    BaseApiResponse<Map<String, Object>> baseApiResponse = BaseApiResponse.success(
        "Logout successfull",
        200,
        data);

    ResponseEntity<BaseApiResponse<Map<String, Object>>> responseEntity = new ResponseEntity<>(baseApiResponse,
        HttpStatus.OK);

    return responseEntity;
  }

  private Map<String, Object> _buildClaims(String userId, String email, String deviceId, String jti, String tokenType,
      String userAgent, String ipAddress, String location) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("tokenType", tokenType);
    claims.put("userId", userId);
    claims.put("email", email);
    claims.put("deviceId", deviceId);
    claims.put("jti", jti);
    claims.put("userAgent", userAgent);
    claims.put("ipAddress", ipAddress);
    claims.put("location", location);
    return claims;
  }
}
