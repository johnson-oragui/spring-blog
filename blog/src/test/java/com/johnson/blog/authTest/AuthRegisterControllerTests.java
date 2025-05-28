package com.johnson.blog.authTest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.johnson.utilities.dtos.authDtos.UserRegistrationDto;

public class AuthRegisterControllerTests extends BaseAuthControllerTest {

  @Test
  public void whenValidRegistration_thenReturn201() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        TEST_FIRSTNAME,
        TEST_EMAIL,
        TEST_PASSWORD,
        TEST_CONFIRMPASSWORD);

    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRegistrationDto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.message").value("User registered successfully"))
        .andExpect(jsonPath("$.status").value(201))
        .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
        .andExpect(jsonPath("$.data.firstname").value(TEST_FIRSTNAME))
        .andExpect(jsonPath("$.data.id").isString());
  }

  @Test
  public void whenEmailAlreadyTaken_thenReturn209() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        TEST_FIRSTNAME,
        "johnson@email.com",
        TEST_PASSWORD,
        TEST_CONFIRMPASSWORD);

    // register successful with email
    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRegistrationDto)))
        .andExpect(status().isCreated());

    // try register with same email
    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRegistrationDto)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Email already in use"))
        .andExpect(jsonPath("$.status").value(409));
  }

  @Test
  public void whenMissingFirstname_thenReturns422() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        null,
        TEST_EMAIL,
        TEST_PASSWORD,
        TEST_CONFIRMPASSWORD);

    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRegistrationDto)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
        .andExpect(jsonPath("$.error").value("Validation error"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.data").value(new HashMap<>(Map.of("firstname", "Firstname is required"))));
  }

  @Test
  public void whenFirstnameHasLessThan3Chars_thenReturns422() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        "fi",
        TEST_EMAIL,
        TEST_PASSWORD,
        TEST_CONFIRMPASSWORD);

    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRegistrationDto)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
        .andExpect(jsonPath("$.error").value("Validation error"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.data")
            .value(new HashMap<>(Map.of("firstname", "firstname must have lenght up to 3 and not more than 100"))));
  }

  @Test
  public void whenFirstnameHasSpecialChars_thenReturns422() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        "fi@name",
        TEST_EMAIL,
        TEST_PASSWORD,
        TEST_CONFIRMPASSWORD);

    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRegistrationDto)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
        .andExpect(jsonPath("$.error").value("Validation error"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.data")
            .value(new HashMap<>(Map.of("firstname", "firstname must not include special characters"))));
  }

  @Test
  public void whenEmailIsMissingAndInvalid_thenReturns422() throws Exception {
    // missing email
    UserRegistrationDto missingEmailDto = new UserRegistrationDto(
        TEST_FIRSTNAME,
        null,
        TEST_PASSWORD,
        TEST_CONFIRMPASSWORD);

    // invalid email
    UserRegistrationDto invalidEmailDto = new UserRegistrationDto(
        TEST_FIRSTNAME,
        "invalidemail",
        TEST_PASSWORD,
        TEST_CONFIRMPASSWORD);

    // missing email
    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(missingEmailDto)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
        .andExpect(jsonPath("$.error").value("Validation error"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.data").value(new HashMap<>(Map.of("email", "Email is required"))));

    // invalid email
    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(invalidEmailDto)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
        .andExpect(jsonPath("$.error").value("Validation error"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.data").value(new HashMap<>(Map.of("email", "Email should be valid"))));
  }

  @Test
  public void whenPasswordIsMissing_thenReturns422() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        TEST_FIRSTNAME,
        TEST_EMAIL,
        null,
        TEST_CONFIRMPASSWORD);

    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRegistrationDto)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
        .andExpect(jsonPath("$.error").value("Validation error"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.data")
            .value(new HashMap<>(Map.of("password", "Password is required"))));
  }

  @Test
  public void whenPasswordIsMissingUppercase_thenReturns422() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        TEST_FIRSTNAME,
        TEST_EMAIL,
        "password123#",
        TEST_CONFIRMPASSWORD);

    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRegistrationDto)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
        .andExpect(jsonPath("$.error").value("Validation error"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.data")
            .value(new HashMap<>(Map.of("password",
                "Password must contain at least one digit, one lowercase, one uppercase letter, one special character, and be at least 8 characters long"))));
  }

  @Test
  public void whenPasswordIsMissingLowercase_thenReturns422() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        TEST_FIRSTNAME,
        TEST_EMAIL,
        "PASSWORD123#",
        TEST_CONFIRMPASSWORD);

    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRegistrationDto)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
        .andExpect(jsonPath("$.error").value("Validation error"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.data")
            .value(new HashMap<>(Map.of("password",
                "Password must contain at least one digit, one lowercase, one uppercase letter, one special character, and be at least 8 characters long"))));
  }

  @Test
  public void whenPasswordIsMissingDigit_thenReturns422() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        TEST_FIRSTNAME,
        TEST_EMAIL,
        "Password#",
        TEST_CONFIRMPASSWORD);

    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRegistrationDto)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
        .andExpect(jsonPath("$.error").value("Validation error"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.data")
            .value(new HashMap<>(Map.of("password",
                "Password must contain at least one digit, one lowercase, one uppercase letter, one special character, and be at least 8 characters long"))));
  }

  @Test
  public void whenPasswordIsMissingSpecialChar_thenReturns422() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        TEST_FIRSTNAME,
        TEST_EMAIL,
        "Password1234",
        TEST_CONFIRMPASSWORD);

    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRegistrationDto)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
        .andExpect(jsonPath("$.error").value("Validation error"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.data")
            .value(new HashMap<>(Map.of("password",
                "Password must contain at least one digit, one lowercase, one uppercase letter, one special character, and be at least 8 characters long"))));
  }

  @Test
  public void whenPasswordLenghtLessThan8_thenReturns422() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        TEST_FIRSTNAME,
        TEST_EMAIL,
        "Pa123#",
        TEST_CONFIRMPASSWORD);

    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRegistrationDto)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
        .andExpect(jsonPath("$.error").value("Validation error"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.data")
            .value(new HashMap<>(Map.of("password",
                "Password must contain at least one digit, one lowercase, one uppercase letter, one special character, and be at least 8 characters long"))));
  }

  @Test
  public void whenConfirmPasswordMissing_thenReturns422() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        TEST_FIRSTNAME,
        TEST_EMAIL,
        TEST_PASSWORD,
        null);

    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRegistrationDto)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("One or more fields are invalid"))
        .andExpect(jsonPath("$.error").value("Validation error"))
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.data")
            .value(new HashMap<>(Map.of("confirmPassword",
                "confirmPassword is required"))));
  }

  @Test
  public void whenConfirmPasswordNotEqualToPassword_thenReturns422() throws Exception {
    UserRegistrationDto userRegistrationDto = new UserRegistrationDto(
        TEST_FIRSTNAME,
        TEST_EMAIL,
        TEST_PASSWORD,
        "PasSwOrD1234#");

    mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(userRegistrationDto)))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.message").value("password and confirm password must match"))
        .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
        .andExpect(jsonPath("$.status").value(422));
  }
}
