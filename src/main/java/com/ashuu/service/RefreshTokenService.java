package com.ashuu.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.ashuu.model.Admin;
import com.ashuu.model.RefreshToken;
import com.ashuu.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {

	@Value("${jwt.refresh.expiration-seconds:604800}")
	private long refreshExpirationSeconds;

    private final RefreshTokenRepository repo;

    public RefreshTokenService(RefreshTokenRepository repo) {
        this.repo = repo;
    }

	// ── Create ────────────────────────────────────────────────────────────────
	@Transactional
    public RefreshToken create(Admin admin) {

		// Fix 3: deleteByAdmin deletes ALL rows for this admin in one query
		// Safe even when 27 duplicate rows exist — no uniqueness assumption
		repo.deleteByAdmin(admin);
		repo.flush(); // ensure delete commits before insert to avoid UK violation

        RefreshToken token = new RefreshToken();
        token.setAdmin(admin);
        token.setToken(UUID.randomUUID().toString());
		token.setExpiryDate(Instant.now().plusSeconds(refreshExpirationSeconds));

        return repo.save(token);
    }

	// ── Verify ────────────────────────────────────────────────────────────────
	public RefreshToken verify(String tokenValue) {
		RefreshToken refresh = repo.findByToken(tokenValue)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (refresh.getExpiryDate().isBefore(Instant.now())) {
            repo.delete(refresh);
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired — please log in again");
        }

        return refresh;
    }

	// ── Delete single token (used for rotation) ───────────────────────────────
	@Transactional
	public void delete(RefreshToken token) {
		repo.delete(token);
	}

	// ── Delete by admin (used for logout) ─────────────────────────────────────
	@Transactional
	public void deleteByAdmin(Admin admin) {
		repo.deleteByAdmin(admin);
	}
}