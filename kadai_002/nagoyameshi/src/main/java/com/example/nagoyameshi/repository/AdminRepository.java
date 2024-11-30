package com.example.nagoyameshi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Integer> {
    Optional<Admin> findByEmail(String email);  // メールアドレスで管理者を検索
}
