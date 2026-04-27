package com.ashuu.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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

			helper.setFrom(fromEmail);
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
				    <div style="font-family:Arial; padding:20px;">
				        <h2 style="color:#333;">OTP Verification</h2>
				        <p>Your OTP is:</p>
				        <h1 style="color:#007bff;">%s</h1>
				        <p>This OTP is valid for 5 minutes.</p>
				        <p style="color:red;">Do not share this code with anyone.</p>
				        <br/>
				        <p>Regards,<br/>Admin Team</p>
				    </div>
				""".formatted(otp);
	}
}