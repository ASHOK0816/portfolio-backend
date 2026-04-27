package com.ashuu.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ashuu.config.AppConfig;
import com.ashuu.dto.AuthRequestDTO;
import com.ashuu.dto.OtpVerifyRequest;
import com.ashuu.dto.RefreshTokenRequest;
import com.ashuu.dto.ResetPasswordRequest;
import com.ashuu.dto.SendOtpRequest;
import com.ashuu.dto.SignupRequestDTO;
import com.ashuu.model.Admin;
import com.ashuu.model.OtpPurpose;
import com.ashuu.model.RefreshToken;
import com.ashuu.model.Role;
import com.ashuu.repository.AdminRepository;
import com.ashuu.security.JwtUtil;
import com.ashuu.service.AdminDetailsService;
import com.ashuu.service.OtpService;
import com.ashuu.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AdminRepository adminRepo;
	private final AdminDetailsService adminDetailsService;
	private final PasswordEncoder encoder;
	private final OtpService otpService;
	private final JwtUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;
	private final AppConfig appConfig;

	// ── LOGIN ─────────────────────────────────────────────────────────────
	@PostMapping("/login")
	public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequestDTO req) {

		if (req.getLogin() == null || req.getLogin().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username or Email required");
		}

		if (isBlank(req.getPassword())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password required");
		}

		Admin admin = adminRepo.findByUsername(req.getLogin())
				.or(() -> adminRepo.findByEmail(req.getLogin().toLowerCase()))
				.filter(a -> encoder.matches(req.getPassword(), a.getPassword()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

		String accessToken = jwtUtil.generateToken(admin.getUsername(), // ALWAYS username
				admin.getRole().name());

		String refreshToken = refreshTokenService.create(admin).getToken();

		return ResponseEntity
				.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken, "role", admin.getRole().name()));
	}

	// ── REFRESH ───────────────────────────────────────────────────────────
	@PostMapping("/refresh")
	public ResponseEntity<Map<String, String>> refresh(@RequestBody RefreshTokenRequest request) {

		if (isBlank(request.getRefreshToken())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required");
		}

		RefreshToken token = refreshTokenService.verify(request.getRefreshToken());

		refreshTokenService.delete(token);

		String newAccessToken = jwtUtil.generateToken(token.getAdmin().getUsername(),
				token.getAdmin().getRole().name());

		String newRefreshToken = refreshTokenService.create(token.getAdmin()).getToken();

		return ResponseEntity.ok(Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken));
	}

	// ── SIGNUP ────────────────────────────────────────────────────────────
	@PostMapping("/signup")
	public ResponseEntity<Map<String, String>> signup(@RequestBody SignupRequestDTO request,
			@RequestHeader(value = "X-Signup-Secret", required = false) String signupSecret) {

		if (isBlank(appConfig.getSignupSecret()) || !appConfig.getSignupSecret().equals(signupSecret)) {

			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorised to create admin accounts");
		}

		if (isBlank(request.getUsername())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username required");
		}

		if (isBlank(request.getEmail()) || !request.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {

			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid email required");
		}

		if (isBlank(request.getPassword()) || request.getPassword().length() < 8) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Min 8 char password required");
		}

		if (adminRepo.existsByUsername(request.getUsername())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
		}

		if (adminRepo.existsByEmail(request.getEmail().toLowerCase())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
		}

		Admin admin = new Admin();
		admin.setUsername(request.getUsername().trim());
		admin.setEmail(request.getEmail().trim().toLowerCase());
		admin.setPassword(encoder.encode(request.getPassword()));
		admin.setRole(Role.ROLE_ADMIN);

		adminRepo.save(admin);

		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Admin registered successfully"));
	}

	// ── OTP FLOW ──────────────────────────────────────────────────────────
	@PostMapping("/admin/send-otp")
	public ResponseEntity<String> sendAdminOtp(@RequestBody SendOtpRequest request) {

		if (isBlank(request.getEmail())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email required");
		}

		String email = request.getEmail().trim().toLowerCase();

		Optional<Admin> adminOpt = adminRepo.findByEmail(email);

		if (adminOpt.isPresent()) {
			otpService.sendOtp(email, OtpPurpose.RESET_PASSWORD);
		}

		// 🔒 Prevent user enumeration
		return ResponseEntity.ok("If the account exists, an OTP has been sent");
	}

	@PostMapping("/admin/verify-otp")
	public ResponseEntity<String> verifyAdminOtp(@RequestBody OtpVerifyRequest request) {

		if (isBlank(request.getEmail()) || isBlank(request.getOtp())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and OTP required");
		}

		return ResponseEntity.ok(otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpPurpose.RESET_PASSWORD));
	}

	@PostMapping("/admin/reset-password")
	public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {

		if (isBlank(request.getEmail()) || isBlank(request.getNewPassword())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email & password required");
		}

		if (request.getNewPassword().length() < 8) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Min 8 chars required");
		}

		if (!otpService.isOtpVerified(request.getEmail(), OtpPurpose.RESET_PASSWORD)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "OTP verification required");
		}

		adminDetailsService.resetPassword(request.getEmail(), request.getNewPassword());

		otpService.clearOtpVerification(request.getEmail(), OtpPurpose.RESET_PASSWORD);

		return ResponseEntity.ok("Password updated successfully");
	}

	private boolean isBlank(String v) {
		return v == null || v.isBlank();
	}
}