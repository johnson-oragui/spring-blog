package com.johnson.utilities.dtos.authDtos;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

public record UserDataResponseDto(
                String id,
                String firstname,
                String email,
                @JsonFormat(pattern = "yyy-MM-dd'T'HH:mm:ss") OffsetDateTime createdAt,
                @JsonFormat(pattern = "yyy-MM-dd'T'HH:mm:ss") OffsetDateTime updatedAt

) {

}
