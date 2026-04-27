package com.ashuu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequestDTO {

	@NotBlank(message = "Username or Email is required")
	private String login;

	@NotBlank(message = "Password is required")
    private String password;

}

