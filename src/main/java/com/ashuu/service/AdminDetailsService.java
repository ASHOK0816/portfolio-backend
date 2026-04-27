package com.ashuu.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ashuu.model.Admin;
import com.ashuu.repository.AdminRepository;
import com.ashuu.security.UserPrincipal;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminDetailsService implements UserDetailsService {

	private final AdminRepository adminRepository;

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {

		Admin admin = adminRepository.findByUsername(input).or(() -> adminRepository.findByEmail(input.toLowerCase()))
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		return UserPrincipal.create(admin);
	}

	public void resetPassword(String email, String newPassword) {

		Admin admin = adminRepository.findByEmail(email.toLowerCase())
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		admin.setPassword(new BCryptPasswordEncoder().encode(newPassword));

		adminRepository.save(admin);
	}

}
