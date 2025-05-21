package com.johnson.database;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseManager {
  @Value("${spring.datasource.url}")
  private String dburl;

  @Value("${spring.datasource.username}")
  private String dbUser;

  @Value("${spring.datasource.password}")
  private String dbPassword;

  @Bean
  public DataSource dataSource() {
    return DataSourceBuilder.create().url(dburl).username(dbUser).password(dbPassword).build();
  }

}
