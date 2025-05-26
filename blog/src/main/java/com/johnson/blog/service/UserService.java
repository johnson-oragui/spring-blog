package com.johnson.blog.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.johnson.database.model.UserModel;
import com.johnson.database.repository.UserRepository;
import com.johnson.utilities.dtos.BaseApiResponse;
import com.johnson.utilities.dtos.PaginationMeta;
import com.johnson.utilities.dtos.authDtos.UserDataResponseDto;

@Service
@Transactional
public class UserService {
  private UserRepository userRepository;

  public UserService(
      UserRepository userRepository) {
    this.userRepository = userRepository;
  }

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
