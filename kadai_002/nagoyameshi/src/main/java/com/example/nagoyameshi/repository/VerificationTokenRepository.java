package com.example.nagoyameshi.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;


public interface VerificationTokenRepository extends JpaRepository<VerificationToken,Integer>{
    // ユーザーとタイプ（登録 or パスワードリセット）でトークンを検索
    VerificationToken findByUser(User user);
    
    // トークンを検索
    VerificationToken findByToken(String token);
    
    boolean existsByToken(String token);  // トークンの存在確認メソッドを追加
}