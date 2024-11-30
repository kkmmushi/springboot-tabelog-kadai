package com.example.nagoyameshi.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Admin;
import com.example.nagoyameshi.repository.AdminRepository;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // BCryptPasswordEncoderを注入

    // メールアドレスとパスワードで認証
    public boolean authenticateAdmin(String email, String password) {
        Optional<Admin> adminOptional = adminRepository.findByEmail(email);

        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            // パスワードを照合
            
            System.out.println("マッチ結果："+passwordEncoder.matches(password, admin.getPassword()));
            
            return passwordEncoder.matches(password, admin.getPassword());
            
        }

        return false;  // メールアドレスが存在しない場合
    }
}
