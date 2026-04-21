package com.ashuu.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ashuu.dto.AuthRequestDTO;
import com.ashuu.dto.OtpVerifyRequest;
import com.ashuu.dto.RefreshTokenRequest;
import com.ashuu.dto.ResetPasswordRequest;
import com.ashuu.dto.SendOtpRequest;
import com.ashuu.model.Admin;
import com.ashuu.model.RefreshToken;
import com.ashuu.model.Role;
import com.ashuu.repository.AdminRepository;
import com.ashuu.security.JwtUtil;
import com.ashuu.service.AdminDetailsService;
import com.ashuu.service.OtpService;
import com.ashuu.service.RefreshTokenService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private static final String ADMIN_KEY = "ADMIN_PROFILE";

	private final AdminRepository adminRepo;
	private final AdminDetailsService adminDetailsService;
    private final BCryptPasswordEncoder encoder;
	private final OtpService otpService;
	private final JwtUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;

    public AuthController(AdminRepository adminRepo,
                          BCryptPasswordEncoder encoder,
                          AdminDetailsService adminDetailsService,
                          OtpService otpService,
                          JwtUtil jwtUtil,
                          RefreshTokenService refreshTokenService) {
		this.adminRepo = adminRepo;
		this.encoder = encoder;
        this.adminDetailsService = adminDetailsService;
		this.otpService = otpService;
		this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

	// ── LOGIN ─────────────────────────────────────────────────────────────────
    @PostMapping("/login")
	public ResponseEntity<Map<String, String>> login(@RequestBody AuthRequestDTO req) {

		// Must have password
		if (isBlank(req.getPassword())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
		}

		// Must have either username or email
		boolean hasUsername = !isBlank(req.getUsername());
		boolean hasEmail = !isBlank(req.getEmail());

		if (!hasUsername && !hasEmail) {
            throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Username or email is required"
            );
        }

		// Resolve admin — try username first, fall back to email
		// Same error message either way — prevents user enumeration
		Admin admin = resolveAdmin(req.getUsername(), req.getEmail())
				.filter(a -> encoder.matches(req.getPassword(), a.getPassword()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

		String accessToken = jwtUtil.generateToken(admin.getUsername(), admin.getRole().name());
        String refreshToken = refreshTokenService.create(admin).getToken();

		return ResponseEntity
				.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken, "role", admin.getRole().name()));
	}

	// ── Resolves admin by username OR email ──────────────────────────────────────
	private Optional<Admin> resolveAdmin(String username, String email) {
		if (!isBlank(username)) {
			return adminRepo.findByUsername(username.trim());
		}
		return adminRepo.findByEmail(email.trim().toLowerCase());
    }

	// ── REFRESH ───────────────────────────────────────────────────────────────
    @PostMapping("/refresh")
	public ResponseEntity<Map<String, String>> refresh(@RequestBody RefreshTokenRequest request) {

		if (isBlank(request.getRefreshToken())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required");
		}

		RefreshToken token = refreshTokenService.verify(request.getRefreshToken());

		// Rotate — invalidate old, issue new
		refreshTokenService.delete(token);
		String newRefreshToken = refreshTokenService.create(token.getAdmin()).getToken();
		String newAccessToken = jwtUtil.generateToken(token.getAdmin().getUsername(),
				token.getAdmin().getRole().name());

		return ResponseEntity.ok(Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken));
    }

	// ── SIGNUP ────────────────────────────────────────────────────────────────
	@PostMapping("/signup")
	public ResponseEntity<Map<String, String>> signup(@RequestBody AuthRequestDTO request,
			@RequestHeader(value = "X-Signup-Secret", required = false) String signupSecret) {

		// Gate with server-side secret
		String expectedSecret = System.getProperty("app.signup.secret", "");
		if (expectedSecret.isBlank() || !expectedSecret.equals(signupSecret)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorised to create admin accounts");
		}

		// Validate all three fields
		if (isBlank(request.getUsername())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
		}
		if (isBlank(request.getEmail()) || !isValidEmail(request.getEmail())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid email address is required");
		}
		if (isBlank(request.getPassword()) || request.getPassword().length() < 8) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
		}

		// Check uniqueness for both username and email
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
		admin.setRole(Role.ADMIN);
		adminRepo.save(admin);

		return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Admin registered successfully"));
	}

	// ── SEND OTP ──────────────────────────────────────────────────────────────
    @PostMapping("/admin/send-otp")
	public ResponseEntity<String> sendAdminOtp(@RequestBody SendOtpRequest request) {

		if (isBlank(request.getEmail())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Valid Email is required");
		}

		return ResponseEntity.ok(otpService.sendAdminOtp(request.getEmail()));
    }

	// ── VERIFY OTP ────────────────────────────────────────────────────────────
    @PostMapping("/admin/verify-otp")
	public ResponseEntity<String> verifyAdminOtp(@RequestBody OtpVerifyRequest request) {

		if (isBlank(request.getEmail()) || isBlank(request.getOtp())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and OTP are required");
		}

        return ResponseEntity.ok(
				otpService.verifyAdminOtp(request.getEmail(), request.getOtp())
        );
    }

	// ── RESET PASSWORD ────────────────────────────────────────────────────────
    @PostMapping("/admin/reset-password")
	public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {

		if (isBlank(request.getEmail()) || isBlank(request.getNewPassword())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and new password are required");
		}
		if (request.getNewPassword().length() < 8) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
		}

		// Enforce OTP flow order — must verify OTP before resetting
		if (!otpService.isOtpVerified(request.getEmail())) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"OTP verification required before resetting password");
		}

		adminDetailsService.resetPassword(request.getEmail(), request.getNewPassword());
		otpService.clearOtpVerification(request.getEmail());

		return ResponseEntity.ok("Password updated successfully");
	}

	// ── Helpers ───────────────────────────────────────────────────────────────
	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private boolean isValidEmail(String email) {
		return email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }
}