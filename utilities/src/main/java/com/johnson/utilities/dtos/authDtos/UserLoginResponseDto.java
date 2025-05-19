package com.johnson.utilities.dtos.authDtos;

import java.util.Map;

public record UserLoginResponseDto(
    Map<String, Object> accessToken,
    UserDataResponseDto userData

) {
}
