package com.ashuu.service.impl;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ashuu.model.Admin;
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

	// username → OTP entry
	private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
	// username → verified-until Instant
	private final Map<String, Instant> verifiedStore = new ConcurrentHashMap<>();

	public OtpServiceImpl(AdminRepository adminRepository, JavaMailSender mailSender) {
		this.adminRepository = adminRepository;
		this.mailSender = mailSender;
    }

	// ── Step 1 ────────────────────────────────────────────────────────────────
    @Override
	public String sendAdminOtp(String email) {

		// Look up admin — email lives on the Admin entity
		Admin admin = adminRepository.findByUsername(email).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No admin found with username: " + email));

		String otp = String.format("%06d", (int) (Math.random() * 1_000_000));
		otpStore.put(email, new OtpEntry(otp, Instant.now().plusSeconds(otpExpirySeconds)));

		// Clear any leftover verified flag from a previous flow
		verifiedStore.remove(email);

		sendEmail(admin.getEmail(), // ← from Admin entity
				"Your Admin OTP", "Your OTP code is: " + otp + "\n\nIt expires in " + (otpExpirySeconds / 60)
						+ " minutes." + "\nDo not share this code with anyone.");

		return "OTP sent to " + maskEmail(admin.getEmail());
	}

	// ── Step 2 ────────────────────────────────────────────────────────────────
	@Override
	public String verifyAdminOtp(String email, String otp) {

		OtpEntry entry = otpStore.get(email);

		// Single error — don't reveal whether email or OTP was wrong
		if (entry == null || Instant.now().isAfter(entry.expiry()) || !entry.otp().equals(otp)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }

		// Consume OTP so it cannot be reused
		otpStore.remove(email);

		// Open the verified window for reset-password
		verifiedStore.put(email, Instant.now().plusSeconds(verifiedWindowSeconds));

		return "OTP verified successfully";
	}

	// ── Step 3 guard ──────────────────────────────────────────────────────────
	@Override
	public boolean isOtpVerified(String email) {
		Instant expiry = verifiedStore.get(email);
		if (expiry == null)
			return false;
		if (Instant.now().isAfter(expiry)) {
			verifiedStore.remove(email);
			return false;
		}
		return true;
    }

	// ── Step 3 cleanup ────────────────────────────────────────────────────────
    @Override
	public void clearOtpVerification(String email) {
		verifiedStore.remove(email);
	}

	// ── Mail ──────────────────────────────────────────────────────────────────
	private void sendEmail(String to, String subject, String body) {
		try {
			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setTo(to);
			msg.setSubject(subject);
			msg.setText(body);
			mailSender.send(msg);
		} catch (Exception e) {
			System.err.println("Mail send failed to " + to + ": " + e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP email");
        }
	}

	// Masks "admin@example.com" → "a***@example.com"
	private String maskEmail(String email) {
		if (email == null || !email.contains("@"))
			return "***";
		String[] parts = email.split("@", 2);
		return parts[0].charAt(0) + "***@" + parts[1];
    }

	// Holds OTP code + its expiry timestamp
	private record OtpEntry(String otp, Instant expiry) {
	}
}