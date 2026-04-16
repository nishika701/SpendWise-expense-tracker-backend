package com.SpendWise.ExpenseTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.SpendWise.ExpenseTracker.repository")
public class ExpenseTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpenseTrackerApplication.class, args);
	}

}
