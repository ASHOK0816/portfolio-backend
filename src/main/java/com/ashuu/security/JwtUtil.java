package com.ashuu.security;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private final Key key;
    private final long expiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {

		// Fix 6: validate key length before constructing — gives a clear error
		// at startup instead of a cryptic WeakKeyException later
		byte[] keyBytes = Base64.getDecoder().decode(secret);
		if (keyBytes.length < 32) {
			throw new IllegalArgumentException("jwt.secret must be at least 256 bits (32 bytes) when Base64-decoded. "
					+ "Generate one with: openssl rand -base64 32");
		}

		this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

	// ── Generate ──────────────────────────────────────────────────────────────
	public String generateToken(String username, String role) {

		// Fix 1: store role WITHOUT "ROLE_" prefix — the filter adds it on read
		// Storing "ADMIN" here + filter adding "ROLE_" = correct "ROLE_ADMIN"
		// Old code stored "ROLE_ADMIN" + filter added "ROLE_" = broken
		// "ROLE_ROLE_ADMIN"
		String cleanRole = role.startsWith("ROLE_") ? role.substring(5) : role;

        return Jwts.builder()
				.setSubject(username).claim("role", cleanRole) // stored as "ADMIN"
				.setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + expiration))
				.signWith(key, SignatureAlgorithm.HS256).compact();
    }

	// ── Extract username ──────────────────────────────────────────────────────
    public String extractUsername(String token) {
		// Fix 3: reuses extractClaims() — single parse
		return extractClaims(token).getSubject();
	}

	// ── Extract role ──────────────────────────────────────────────────────────
	public String extractRole(String token) {
		// Fix 3: was parsing independently — now reuses extractClaims()
		// Fix 4: null check — throws clear error if role claim is missing
		String role = extractClaims(token).get("role", String.class);
		if (role == null) {
			throw new IllegalStateException(
					"JWT is missing 'role' claim — token may be malformed or from an old session");
		}
		return role;
	}

	// ── Extract expiration ────────────────────────────────────────────────────
	public Date extractExpiration(String token) {
		return extractClaims(token).getExpiration();
    }

	// ── Validate ──────────────────────────────────────────────────────────────
    public boolean isTokenValid(String token) {
        try {
			// Fix 2: parse claims ONCE and reuse — no double parse
			Claims claims = extractClaims(token);

			// Check expiry directly from already-parsed claims
			return claims.getExpiration().after(new Date());

        } catch (Exception e) {
			// Covers expired, malformed, tampered, and wrong-key tokens
            return false;
        }
    }

	// ── Internal — single parse point for all extractions ────────────────────
	private Claims extractClaims(String token) {
		// Fix 2 + 3: all public methods funnel through here — token parsed once
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}
}