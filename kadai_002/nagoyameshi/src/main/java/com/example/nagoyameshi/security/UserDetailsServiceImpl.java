package com.example.nagoyameshi.security;

 import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;

 @Service
 public class UserDetailsServiceImpl implements UserDetailsService{
	 private final UserRepository userRepository;
	 private final PasswordEncoder passwordEncoder; //パスワードエンコーダ
	

	 public UserDetailsServiceImpl(UserRepository userRepository,PasswordEncoder passwordEncoder) {
		 this.userRepository=userRepository;
		 this.passwordEncoder = passwordEncoder;
		 
	 }
	 
	 @Override
	 public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
		 System.out.println("ユーザー検索: " + email); // デバッグメッセージ
			User user =userRepository.findByEmail(email);
			
			
			if(user==null) {
				throw new UsernameNotFoundException("ユーザーが見つかりませんでした");
			}
			
			String userRoleName = user.getRole();
			Collection<GrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority(userRoleName));
			return new UserDetailsImpl(user, authorities);
		} 
		
		public boolean authenticate(String email,String rawPassword) {
			 System.out.println("認証メソッドが呼ばれました: " + email);
			User user =userRepository.findByEmail(email);
			if(user==null) {
				 System.out.println("ユーザーが見つかりませんでした: " + email);
				return false;
			}
			
			boolean passwordMatches = passwordEncoder.matches(rawPassword, user.getPassword());
			
		    if (!passwordMatches) {
		        System.out.println("パスワードが一致しません: " + rawPassword);
		    } else {
		        System.out.println("パスワードが一致しました");
		    }
			return passwordMatches;
		}
		
		
	 }
 