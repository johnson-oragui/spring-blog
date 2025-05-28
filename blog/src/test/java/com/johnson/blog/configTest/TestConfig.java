package com.johnson.blog.configTest;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@Profile("test")
public class TestConfig {

  @Bean
  @Primary
  public MailSender testMailSender() {
    return new JavaMailSenderImpl(); // Mock or real implementation
  }

  @Bean
  @Primary
  public RedisConnectionFactory testRedisFactory() {
    return new LettuceConnectionFactory("localhost", 6379);
  }

  @Bean
  @Primary // Overrides the real mail sender in test context
  public JavaMailSender javaMailSender() {
    return mock(JavaMailSender.class);
  }

}