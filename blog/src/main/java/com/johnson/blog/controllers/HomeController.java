package com.johnson.blog.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.johnson.utilities.dtos.BaseApiResponse;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class HomeController {

  @GetMapping("")
  public ResponseEntity<BaseApiResponse<Map<String, Object>>> home() {
    BaseApiResponse<Map<String, Object>> response = BaseApiResponse.success(
        "Welcome to Johnson Blog API",
        200,
        new HashMap<>());
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
