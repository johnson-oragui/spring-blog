package com.johnson.blog.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
  @Bean
  public OpenAPI apiInfo() {
    Contact contact = new Contact();
    contact.email("johnson.oragui@gmail.com");
    return new OpenAPI()
        .info(new Info()
            .title("Blog API")
            .description("API documentation for the Blog application")
            .version("1.0.0").contact(contact))
        .servers(List.of(new Server().url("http://localhost:7005")));
  }
}