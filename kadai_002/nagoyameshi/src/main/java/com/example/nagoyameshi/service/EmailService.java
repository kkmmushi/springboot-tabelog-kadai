package com.example.nagoyameshi.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;

    // コンストラクタインジェクション
    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    // メール送信メソッド
    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);               // 送信先
        message.setSubject(subject);     // 件名
        message.setText(text);           // 本文
        javaMailSender.send(message);    // メール送信
    }
}
