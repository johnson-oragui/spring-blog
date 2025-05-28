package com.johnson.blog.authTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.johnson.blog.configTest.TestConfig;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestConfig.class)
public abstract class BaseAuthControllerTest {
  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

  protected final String REGISTER_URL = "/api/v1/auth/register";
  protected final String LOGIN_URL = "/api/v1/auth/login";
  protected final String REFRESH_URL = "/api/v1/auth/refresh";
  protected final String LOGOUT_URL = "/api/v1/auth/logout";

  protected final String TEST_EMAIL = "testuser@example.com";
  protected final String TEST_PASSWORD = "SecurePass123!";
  protected final String TEST_CONFIRMPASSWORD = "SecurePass123!";
  protected final String TEST_FIRSTNAME = "Test";
}
