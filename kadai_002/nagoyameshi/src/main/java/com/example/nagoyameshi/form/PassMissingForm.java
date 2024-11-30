package com.example.nagoyameshi.form;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class PassMissingForm{
	@NotBlank(message="メールアドレスは必須です")
	@Email(message="有効なメールアドレスを入力してください")
	private String email;

	
    @Override
    public String toString() {
        return "PassmissingForm{" +
               "email='" + email + '\'' +
               '}';
    }

	
}