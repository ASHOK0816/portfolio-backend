package com.ashuu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;

@Configuration
@Getter
public class AppConfig {

	@Value("${app.signup.secret}")
	private String signupSecret;
}