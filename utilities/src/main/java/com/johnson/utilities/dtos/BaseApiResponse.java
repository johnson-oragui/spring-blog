package com.johnson.utilities.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BaseApiResponse<T>(
    String message,
    Integer status,
    String error,
    T data,
    PaginationMeta meta) {

  public static <T> BaseApiResponse<T> success(String message, Integer status, T data) {
    return new BaseApiResponse<>(message, status, null, data, null);
  }

  public static <T> BaseApiResponse<T> failure(String message, Integer status, String error) {
    return new BaseApiResponse<>(message, status, error, null, null);
  }

  public static <T> BaseApiResponse<T> successWithPagination(String message, Integer status, T data,
      PaginationMeta meta) {
    return new BaseApiResponse<>(message, status, null, data, meta);
  }
}
