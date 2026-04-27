package com.ashuu.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ashuu.model.Otp;
import com.ashuu.model.OtpPurpose;

public interface OtpRepository extends JpaRepository<Otp, Long> {

	// 🔹 Get latest OTP for email + purpose
	Optional<Otp> findTopByEmailAndPurposeOrderByExpiryTimeDesc(String email, OtpPurpose purpose);

	// 🔹 Delete old OTPs (cleanup)
	void deleteByEmailAndPurpose(String email, OtpPurpose purpose);

	// 🔹 Optional: delete all OTPs for email
	void deleteByEmail(String email);
}