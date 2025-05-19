package com.johnson.utilities.dtos;

public record PaginationMeta(
    int page,
    int size,
    long totalItems,
    int totalPages) {

}
