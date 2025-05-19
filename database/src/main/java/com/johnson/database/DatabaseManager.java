package com.johnson.database;

import javax.sql.DataSource;

// import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.johnson.utilities.config.ConfigUtils;

@Configuration
public class DatabaseManager {
  // @Value("${spring.datasource.url}")
  private String dburl = ConfigUtils.DB_URL;

  // @Value("${spring.datasource.username}")
  private String dbUser = ConfigUtils.DB_USER;

  // @Value("${spring.datasource.password}")
  private String dbPassword = ConfigUtils.DB_PASSWORD;

  @Bean
  public DataSource dataSource() {
    return DataSourceBuilder.create().url(dburl).username(dbUser).password(dbPassword).build();
  }

}
