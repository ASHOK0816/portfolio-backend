package com.ashuu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String fromEmail;

	// 🔥 Generic HTML Email Sender
	public void sendEmail(String to, String subject, String htmlContent) {

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setFrom(new InternetAddress(fromEmail, "Admin Team"));
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlContent, true); // true = HTML

			mailSender.send(message);

			log.info("✅ Email sent successfully to {}", to);

		} catch (Exception ex) {
			log.error("❌ FULL EMAIL ERROR:", ex);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send email");
		}
	}

	// 🔥 OTP Email (Styled HTML)
	public void sendOtpEmail(String email, String otp) {
		String html = buildOtpTemplate(otp);
		sendEmail(email, "🔐 OTP Verification", html);
	}

	// 🎨 Modern HTML Template
	private String buildOtpTemplate(String otp) {
		return """
				<!DOCTYPE html>
				<html>
				<head>
				    <meta charset="UTF-8">
				    <title>OTP Verification</title>
				</head>
				<body style="margin:0;padding:0;background-color:#f4f6f9;font-family:Arial,sans-serif;">

				    <table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 0;">
				        <tr>
				            <td align="center">

				                <table width="600" cellpadding="0" cellspacing="0"
				                    style="background:#ffffff;border-radius:16px;
				                           box-shadow:0 4px 20px rgba(0,0,0,0.08);
				                           overflow:hidden;">

				                    <!-- Header -->
				                    <tr>
				                        <td align="center"
				                            style="background:linear-gradient(135deg,#4f46e5,#7c3aed);
				                                   padding:30px;">
				                            <h1 style="margin:0;color:white;font-size:28px;">
				                                🔐 OTP Verification
				                            </h1>
				                        </td>
				                    </tr>

				                    <!-- Content -->
				                    <tr>
				                        <td style="padding:40px;">

				                            <h2 style="margin-top:0;color:#111827;">
				                                Hello,
				                            </h2>

				                            <p style="font-size:16px;color:#4b5563;line-height:1.7;">
				                                We received a request to verify your account.
				                                Use the OTP below to continue:
				                            </p>

				                            <!-- OTP Box -->
				                            <div style="text-align:center;margin:35px 0;">
				                                <span style="
				                                    display:inline-block;
				                                    background:#eef2ff;
				                                    color:#4f46e5;
				                                    padding:18px 40px;
				                                    border-radius:12px;
				                                    font-size:34px;
				                                    font-weight:bold;
				                                    letter-spacing:8px;
				                                    border:2px dashed #4f46e5;
				                                    user-select:all;">
				                                    %s
				                                </span>

				                                <div style="
				                                            margin-top:10px;
				                                            color:#6b7280;
				                                            font-size:13px;">
				                                            Tap and hold to copy OTP
				                                        </div>

				                            </div>

				                            <p style="font-size:15px;color:#6b7280;">
				                                ⏳ This OTP is valid for
				                                <strong>5 minutes</strong>.
				                            </p>

				                            <p style="
				                                background:#fef2f2;
				                                color:#dc2626;
				                                padding:15px;
				                                border-radius:8px;
				                                font-size:14px;">
				                                ⚠️ Never share this OTP with anyone.
				                                Our team will never ask for your verification code.
				                            </p>

				                        </td>
				                    </tr>

				                    <!-- Footer -->
				                    <tr>
				                        <td align="center"
				                            style="background:#f9fafb;
				                                   padding:25px;
				                                   border-top:1px solid #e5e7eb;">

				                            <p style="margin:0;color:#6b7280;font-size:14px;">
				                                Regards,
				                            </p>

				                            <h3 style="margin:8px 0 0;color:#111827;">
				                                Admin Team
				                            </h3>

				                            <p style="margin-top:10px;color:#9ca3af;font-size:12px;">
				                                This is an automated email. Please do not reply.
				                            </p>

				                        </td>
				                    </tr>

				                </table>

				            </td>
				        </tr>
				    </table>

				</body>
				</html>
				""".formatted(otp);
	}
}