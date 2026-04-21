package com.ashuu.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.ashuu.model.Admin;
import com.ashuu.model.RefreshToken;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

	Optional<RefreshToken> findByAdmin(Admin admin);

	@Modifying
	void deleteByAdmin(Admin admin);
}
