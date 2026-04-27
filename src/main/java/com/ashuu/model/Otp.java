package com.ashuu.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	private String email;

    private String otp;

	@Enumerated(EnumType.STRING)
	private OtpPurpose purpose; // ✅ IMPORTANT

	private boolean verified; // ✅ track verification

    private int resendCount;

    private LocalDateTime expiryTime;

    private LocalDateTime lastSentTime;
}
