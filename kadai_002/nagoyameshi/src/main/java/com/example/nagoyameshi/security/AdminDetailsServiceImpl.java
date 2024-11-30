package com.example.nagoyameshi.security;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.nagoyameshi.entity.Admin;
import com.example.nagoyameshi.repository.AdminRepository;

public class AdminDetailsServiceImpl implements UserDetailsService {
    private final AdminRepository adminRepository;

    public AdminDetailsServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Admin> adminOptional = adminRepository.findByEmail(email);
        
        // Optionalが空の場合に例外をスロー
        if (!adminOptional.isPresent()) {
            throw new UsernameNotFoundException("Admin not found with username: " + email);
        }

        // Optionalの中身を取得
        Admin admin = adminOptional.get();  // 値が存在すると確定しているので get() で取得

        // AdminDetailsImpl を返す
        return new AdminDetailsImpl(admin.getEmail(), admin.getPassword()); // admin.getEmail() と admin.getPassword() を呼び出す
    }
}
