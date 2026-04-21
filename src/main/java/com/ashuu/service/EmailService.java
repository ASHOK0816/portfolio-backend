package com.ashuu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	@Autowired
    private JavaMailSender mailSender;

	public void sendOtp(String email, String otp) {

	    SimpleMailMessage message = new SimpleMailMessage();
	    message.setFrom("ashokpawar514356@gmail.com"); // ADD THIS
	    message.setTo(email);
	    message.setSubject("Admin OTP Verification");
	    message.setText(
	        "Welcome Ashok,\n\n" +
	        "Your OTP is: " + otp +
	        "\nValid for 5 minutes. Do not share this OTP.\n\n" +
	        "Thank You!"
	    );

	    mailSender.send(message);
	}

    
}
