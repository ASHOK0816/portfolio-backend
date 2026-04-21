package com.ashuu.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ashuu.model.Otp;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByUsername(String username);
    Optional<Otp> findByUsernameAndOtp(String username, String otp);

    void deleteByUsername(String username);
}
