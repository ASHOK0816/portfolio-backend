package com.ashuu.dto;

import lombok.Data;

@Data
public class OtpVerifyRequest {

	private String email;
	private String username;
    private String otp;

}
