package com.ashuu.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Data
@Table(name = "refresh_token",
		// Fix: unique constraint prevents multiple tokens per admin at DB level
		uniqueConstraints = @UniqueConstraint(name = "uk_refresh_token_admin", columnNames = "admin_id"))
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(nullable = false, unique = true)
    private String token;

    private Instant expiryDate;

	@OneToOne
	@JoinColumn(name = "admin_id", referencedColumnName = "id", nullable = false)
    private Admin admin;

	private Instant expriryDate;

}

