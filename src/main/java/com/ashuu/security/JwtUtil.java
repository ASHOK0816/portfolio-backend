package com.ashuu.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long expiration;

	// 🔑 key
	private SecretKey key() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
	}

	// ✅ generate token
	public String generateToken(String username, String role) {
		return Jwts.builder().subject(username).claim("role", role).issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + expiration)).signWith(key()).compact();
	}

	// ✅ validate
	public boolean validateToken(String token) {
		try {
			getClaims(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// ✅ username
	public String extractUsername(String token) {
		return getClaims(token).getSubject();
    }

	// ✅ claims
	private Claims getClaims(String token) {
		return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
    }
}