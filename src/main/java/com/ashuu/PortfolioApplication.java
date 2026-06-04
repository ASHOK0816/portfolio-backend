package com.ashuu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PortfolioApplication {

	public static void main(String[] args) {
		System.out.println("NEW BUILD");
		SpringApplication.run(PortfolioApplication.class, args);
	}

}
