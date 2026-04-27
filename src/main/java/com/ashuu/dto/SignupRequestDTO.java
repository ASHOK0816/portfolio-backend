package com.ashuu.dto;

import lombok.Data;

@Data
public class SignupRequestDTO {

	private String username;
	private String email;
	private String password;
}