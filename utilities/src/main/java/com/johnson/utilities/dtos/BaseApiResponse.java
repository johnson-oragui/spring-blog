package com.johnson.utilities.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BaseApiResponse<T>(
    String message,
    String status,
    String error,
    T data,
    PaginationMeta meta) {

  public static <T> BaseApiResponse<T> success(String message, T data) {
    return new BaseApiResponse<>(message, "success", null, data, null);
  }

  public static <T> BaseApiResponse<T> failure(String message, String error) {
    return new BaseApiResponse<>(message, "fail", error, null, null);
  }

  public static <T> BaseApiResponse<T> successWithPagination(String message, T data, PaginationMeta meta) {
    return new BaseApiResponse<>(message, "success", null, data, meta);
  }
}
