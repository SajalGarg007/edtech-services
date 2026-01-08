package com.task.edtech.services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.task.edtech"  // Scans all packages: db.entity, db.repository, db.service, services.controller
})
@EnableJpaRepositories(basePackages = "com.task.edtech.db.repository")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}

