package com.ashuu.service;

import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.ashuu.model.Admin;
import com.ashuu.repository.AdminRepository;

@Service
public class AdminDetailsService implements UserDetailsService {

    private final AdminRepository adminRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminDetailsService(AdminRepository adminRepo, BCryptPasswordEncoder passwordEncoder ) {
        this.adminRepo = adminRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        return adminRepo.findByUsername(username)
            .map(admin -> User.builder()
                .username(admin.getUsername())
                .password(admin.getPassword())
                .roles("ADMIN")
                .build()
            )
            .orElseThrow(() ->
                new UsernameNotFoundException("Admin not found")
            );
    }
    
    public void resetPassword(String username, String newPassword) {

    	Admin admin = adminRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        admin.setPassword(passwordEncoder.encode(newPassword));
        adminRepo.save(admin);
    }

}
