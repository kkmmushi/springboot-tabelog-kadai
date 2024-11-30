package com.example.nagoyameshi.event;

 import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.VerificationTokenService;
 
 @Component
 public class SignupEventListener{
	 private final VerificationTokenService verificationTokenService;
	 private final JavaMailSender javaMailSender;
	 
	 public SignupEventListener(VerificationTokenService verificationTokenService,JavaMailSender mailsender) {
		this.verificationTokenService = verificationTokenService;
		this.javaMailSender = mailsender;
		
	 }
	 
	 @EventListener
	 private void onSignupEvent(SignupEvent signupEvent)
	 {
		 User user = signupEvent.getUser();
	
		 String token = verificationTokenService.createAndReturnToken(user);
		 
		 String subject="メール認証";
		 String confirmationUrl = signupEvent.getRequestUrl()+ "/verify?token=" + token;
		 String recipientAddress = user.getEmail();
		 String message = "以下リンクをクリックし、会員登録を完了してください";
		 
         SimpleMailMessage mailMessage = new SimpleMailMessage(); 
         mailMessage.setTo(recipientAddress);
         mailMessage.setSubject(subject);
         mailMessage.setText(message + "\n" + confirmationUrl);
         // メール送信前にログを追加
         System.out.println("Sending email to: " + recipientAddress);
         javaMailSender.send(mailMessage);
         System.out.println("Email sent successfully");
		 
		 }
	 }