package com.example.nagoyameshi.form;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
 public class SignupForm{
	@NotBlank(message="氏名を入力してください")
	private String name;

    
    @NotBlank(message = "電話番号を入力してください。")
    private String phoneNumber;
    
    @NotBlank(message = "メールアドレスを入力してください。")
    @Email(message = "メールアドレスは正しい形式で入力してください。")
    private String email;
    
    @NotBlank(message = "パスワードを入力してください。")
    @Size(min = 8, message = "パスワードは8文字以上で入力してください。")
    private String password;    
    
    @NotBlank(message = "パスワード（確認用）を入力してください。")
    private String passwordConfirmation;    
}
	
