package com.ashuu.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ashuu.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);

	Optional<Admin> findByEmail(String email);

	boolean existsByUsername(String username);

	boolean existsByEmail(String email);
}

