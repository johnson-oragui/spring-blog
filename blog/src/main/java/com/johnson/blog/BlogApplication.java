package com.johnson.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.johnson.utilities.config.ConfigUtils;

// @SpringBootApplication(scanBasePackages = { "blog", "database", "utilities" })
@SpringBootApplication
@ComponentScan(basePackages = "com.johnson")
@EnableJpaRepositories(basePackages = "com.johnson.database.repository")
@EntityScan(basePackages = "com.johnson.database.model")
public class BlogApplication {

	public static void main(String[] args) {
		// Load .env file
		ConfigUtils.load();
		SpringApplication.run(BlogApplication.class, args);
	}
}
