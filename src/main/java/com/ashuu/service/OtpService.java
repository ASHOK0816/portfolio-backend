package com.ashuu.service;

import com.ashuu.model.OtpPurpose;

public interface OtpService {

	/**
	 * Step 1 — Generates an OTP and sends it to the email address associated with
	 * the given username.
	 *
	 * @param username the admin username
	 * @return confirmation message (e.g. "OTP sent to a***@example.com")
	 * @throws org.springframework.web.server.ResponseStatusException 404 if no
	 *                                                                admin exists
	 *                                                                with this
	 *                                                                username
	 */
	String sendOtp(String email, OtpPurpose purpose);

	/**
	 * Step 2 — Validates the OTP for the given username. On success, internally
	 * marks the username as OTP-verified so that {@link #isOtpVerified(String)}
	 * returns true for a short window.
	 *
	 * @param username the admin username
	 * @param otp      the OTP code submitted by the user
	 * @return confirmation message (e.g. "OTP verified successfully")
	 * @throws org.springframework.web.server.ResponseStatusException 400 if the OTP
	 *                                                                is invalid or
	 *                                                                expired
	 */
	String verifyOtp(String email, String otp, OtpPurpose purpose);

	/**
	 * Step 3 guard — Returns true only if {@link #verifyAdminOtp(String, String)}
	 * was successfully called for this username within the allowed time window.
	 * Used by the reset-password endpoint to enforce the OTP flow order.
	 *
	 * @param username the admin username
	 * @return true if OTP was verified and the verification window is still open
	 */
	boolean isOtpVerified(String email, OtpPurpose purpose);

	/**
	 * Step 3 cleanup — Clears the OTP-verified flag after a successful password
	 * reset so the same verification cannot be reused.
	 *
	 * @param username the admin username
	 */
	void clearOtpVerification(String email, OtpPurpose purpose);
}