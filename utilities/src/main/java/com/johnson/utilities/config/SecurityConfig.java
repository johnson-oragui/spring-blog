package com.johnson.utilities.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

// import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity // is a crucial annotation that imports the necessary Spring Security
                   // configuration. It sets up the infrastructure beans, including the components
                   // responsible for creating and providing the HttpSecurity object that your
                   // @Bean method needs.
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    // http.csrf(csrf -> csrf.disable())
    // .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
    // return http.build();

    // http.csrf(csrf -> csrf.disable())
    // .authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
    // .httpBasic(withDefaults()); // enable basic auth
    // return http.build();

    http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
        .httpBasic(httpBasic -> httpBasic.disable()) // Disable HTTP Basic Auth explicitly
        .formLogin(form -> form.disable()); // Disable form login explicitly
    return http.build();
  }
}
