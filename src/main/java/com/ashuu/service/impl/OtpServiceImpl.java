package com.ashuu.service.impl;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ashuu.model.OtpPurpose;
import com.ashuu.repository.AdminRepository;
import com.ashuu.service.OtpService;

@Service
public class OtpServiceImpl implements OtpService {

	@Value("${app.otp.expiry-seconds:300}")
	private long otpExpirySeconds;

	@Value("${app.otp.verified-window-seconds:300}")
	private long verifiedWindowSeconds;

	private final AdminRepository adminRepository;
	private final JavaMailSender mailSender;

	// key → OTP entry
	private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

	// key → verified-until
	private final Map<String, Instant> verifiedStore = new ConcurrentHashMap<>();

	public OtpServiceImpl(AdminRepository adminRepository, JavaMailSender mailSender) {
		this.adminRepository = adminRepository;
		this.mailSender = mailSender;
    }

	// ─────────────────────────────────────────────
	// STEP 1: SEND OTP
	// ─────────────────────────────────────────────
	@Override
	public String sendOtp(String email, OtpPurpose purpose) {

		String normalizedEmail = email.trim().toLowerCase();
		String key = buildKey(normalizedEmail, purpose);

		boolean userExists = adminRepository.findByEmail(normalizedEmail).isPresent();

		// Prevent user enumeration
		if (purpose == OtpPurpose.RESET_PASSWORD && !userExists) {
			return "If the account exists, an OTP has been sent";
		}

		// Rate limiting (30 seconds)
		OtpEntry existing = otpStore.get(key);
		if (existing != null && Instant.now().isBefore(existing.lastSentTime().plusSeconds(30))) {

			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
					"Wait 30 seconds before requesting another OTP");
		}

		String otp = generateOtp();

		otpStore.put(key, new OtpEntry(otp, Instant.now().plusSeconds(otpExpirySeconds), Instant.now()));

		// Clear previous verification
		verifiedStore.remove(key);

		// Send only if valid
		if (userExists || purpose == OtpPurpose.SIGNUP) {
			sendEmail(normalizedEmail, "Your OTP Code", "Your OTP is: " + otp + "\nExpires in "
					+ (otpExpirySeconds / 60) + " minutes.\n\nDo not share this code.");
		}

		return "If the account exists, an OTP has been sent";
	}

	// ─────────────────────────────────────────────
	// STEP 2: VERIFY OTP
	// ─────────────────────────────────────────────
	@Override
	public String verifyOtp(String email, String otp, OtpPurpose purpose) {

		String key = buildKey(email, purpose);

		OtpEntry entry = otpStore.get(key);

		if (entry == null || Instant.now().isAfter(entry.expiry()) || !entry.otp().equals(otp)) {

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
		}

		// Consume OTP
		otpStore.remove(key);

		// Mark verified
		verifiedStore.put(key, Instant.now().plusSeconds(verifiedWindowSeconds));

		return "OTP verified successfully";
	}

	// ─────────────────────────────────────────────
	// STEP 3: CHECK VERIFIED
	// ─────────────────────────────────────────────
	@Override
	public boolean isOtpVerified(String email, OtpPurpose purpose) {

		String key = buildKey(email, purpose);

		Instant expiry = verifiedStore.get(key);

		if (expiry == null)
			return false;

		if (Instant.now().isAfter(expiry)) {
			verifiedStore.remove(key);
			return false;
		}

		return true;
	}

	// ─────────────────────────────────────────────
	// STEP 4: CLEAR VERIFIED
	// ─────────────────────────────────────────────
	@Override
	public void clearOtpVerification(String email, OtpPurpose purpose) {
		String key = buildKey(email, purpose);
		verifiedStore.remove(key);
	}

	// ─────────────────────────────────────────────
	// UTIL: SEND EMAIL
	// ─────────────────────────────────────────────
	private void sendEmail(String to, String subject, String body) {
		try {
			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setTo(to);
			msg.setSubject(subject);
			msg.setText(body);
			mailSender.send(msg);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP email");
        }
	}

	// ─────────────────────────────────────────────
	// UTIL: GENERATE OTP (SECURE)
	// ─────────────────────────────────────────────
	private String generateOtp() {
		return String.valueOf(100000 + new SecureRandom().nextInt(900000));
    }

	// ─────────────────────────────────────────────
	// UTIL: UNIQUE KEY
	// ─────────────────────────────────────────────
	private String buildKey(String email, OtpPurpose purpose) {
		return email.trim().toLowerCase() + ":" + purpose.name();
	}

	// ─────────────────────────────────────────────
	// INTERNAL RECORD
	// ─────────────────────────────────────────────
	private record OtpEntry(String otp, Instant expiry, Instant lastSentTime) {
	}
}