package com.johnson.utilities.dtos.authDtos;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record UserDataResponseDto(
        String id,
        String firstname,
        String email,
        @JsonFormat(pattern = "yyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyy-MM-dd'T'HH:mm:ss") LocalDateTime updatedAt

) {

}
