package com.example.nagoyameshi.service;

import java.util.UUID;  // UUIDを使用するためにインポート

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.repository.VerificationTokenRepository;

@Service
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;

    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository) {
        this.verificationTokenRepository = verificationTokenRepository;
    }

    // ユーザーごとにトークンを生成して保存し、そのトークンを返すメソッド
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
    public String createAndReturnToken(User user) {
        // 既存のトークンがあれば削除
        VerificationToken existingToken = verificationTokenRepository.findByUser(user);
        if (existingToken != null) {
            verificationTokenRepository.delete(existingToken);  // 既存のトークンを削除
        }

        // 新しいユニークなトークンを生成
        String newToken = generateUniqueToken();  // 新しいユニークなトークンを生成

        // トークンが重複しないように確認
        while (verificationTokenRepository.existsByToken(newToken)) {
            newToken = generateUniqueToken();  // 重複した場合、新しいトークンを再生成
        }

        // 新しいトークンを保存
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setUser(user);
        verificationToken.setToken(newToken);

        verificationTokenRepository.save(verificationToken);  // トークンを保存

        return newToken;  // 新しく生成したトークンを返す
    }

    // 新しいユニークなトークンを生成するメソッド
    private String generateUniqueToken() {
        // UUIDを使用して一意のトークンを生成
        return UUID.randomUUID().toString();
    }

    // トークンを取得するメソッド（トークンに基づく検索）
    public VerificationToken getVerificationToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }
}
