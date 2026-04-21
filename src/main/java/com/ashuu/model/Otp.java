package com.ashuu.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String otp;

    private int resendCount;
    private LocalDateTime expiryTime;
    private LocalDateTime lastSentTime;

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getOtp() {
		return otp;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
	public int getResendCount() {
		return resendCount;
	}
	public void setResendCount(int resendCount) {
		this.resendCount = resendCount;
	}
	public LocalDateTime getExpiryTime() {
		return expiryTime;
	}
	public void setExpiryTime(LocalDateTime expiryTime) {
		this.expiryTime = expiryTime;
	}
	public LocalDateTime getLastSentTime() {
	    return lastSentTime;
	}

	public void setLastSentTime(LocalDateTime lastSentTime) {
	    this.lastSentTime = lastSentTime;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
    
}
