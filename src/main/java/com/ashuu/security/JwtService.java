package com.ashuu.security;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtService {

	private enum TokenType {
		ACCESS, REFRESH
	}

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.expiration}")
	private long accessExpiration;

	@Value("${jwt.refresh.expiration}")
	private long refreshExpiration;

	@Value("${security.jwt.issuer:portfolio-api}")
	private String issuer;

	// ─────────────────────────────
	// TOKEN GENERATION
	// ─────────────────────────────

	public String generateAccessToken(UserDetails user) {
		return buildToken(user, TokenType.ACCESS, accessExpiration);
	}

	public String generateRefreshToken(UserDetails user) {
		return buildToken(user, TokenType.REFRESH, accessExpiration);
	}

	private String buildToken(UserDetails user, TokenType type, long expiry) {

		Instant now = Instant.now();

		return Jwts.builder().id(UUID.randomUUID().toString()).issuer(issuer).subject(user.getUsername())
				.issuedAt(Date.from(now)).expiration(new Date(System.currentTimeMillis() + expiry))
				.claims(createClaims(user, type)).signWith(getSigningKey()).compact();
	}

	private HashMap<String, Object> createClaims(UserDetails user, TokenType type) {
		HashMap<String, Object> claims = new HashMap<>();
		claims.put("roles", extractRoles(user));
		claims.put("type", type.name());
		return claims;
	}

	// ─────────────────────────────
	// VALIDATION
	// ─────────────────────────────

	public boolean isTokenValid(String token, UserDetails user, boolean isAccess) {
		try {
			Claims claims = parseClaims(token);

			String expectedType = isAccess ? TokenType.ACCESS.name() : TokenType.REFRESH.name();

			return claims.getSubject().equals(user.getUsername()) && !isExpired(claims)
					&& expectedType.equals(claims.get("type", String.class));

		} catch (JwtException e) {
			log.debug("JWT invalid: {}", e.getMessage());
			return false;
		}
	}

	public boolean isTokenValid(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException e) {
			return false;
		}
	}

	// ─────────────────────────────
	// EXTRACTION
	// ─────────────────────────────

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public List<String> extractRoles(String token) {
		return extractClaim(token, c -> c.get("roles", List.class));
	}

	public <T> T extractClaim(String token, Function<Claims, T> resolver) {
		return resolver.apply(parseClaims(token));
	}

	// ─────────────────────────────
	// CORE PARSING
	// ─────────────────────────────

	private Claims parseClaims(String token) {
		return Jwts.parser().verifyWith(getSigningKey()).requireIssuer(issuer).build().parseSignedClaims(token)
				.getPayload();
	}

	private boolean isExpired(Claims claims) {
		return claims.getExpiration().before(new Date());
	}

	// ─────────────────────────────
	// SIGNING KEY
	// ─────────────────────────────

	private SecretKey getSigningKey() {

		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException("JWT secret is missing (jwt.secret)");
		}

		try {
			byte[] keyBytes = Decoders.BASE64.decode(secret);
			return Keys.hmacShaKeyFor(keyBytes);

		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("JWT secret must be Base64 encoded", e);
		}
	}

	// ─────────────────────────────
	// HELPERS
	// ─────────────────────────────

	private List<String> extractRoles(UserDetails user) {
		return user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
	}
}