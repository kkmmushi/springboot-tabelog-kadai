package com.example.nagoyameshi.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.nagoyameshi.entity.User;
 
 public class UserDetailsImpl implements UserDetails{
	 private final User user;
	 private final Collection<GrantedAuthority> authorities;
	 
	 public UserDetailsImpl(User user, Collection<GrantedAuthority> authorities) {
		 this.user=user;
		 this.authorities=authorities;
		 
	 }
	 public User getUser() {
		 return user;
	 }
	 
	 //--------------------------ハッシュ化済のパスワードを戻す
	 @Override
	 public String getPassword() {
		 return user.getPassword();
	 }
	 
	 //--------------------------ログインに利用するユーザー名を戻す
	 @Override
	 public String getUsername() {
		 return user.getEmail();
	 }
	 
	 //--------------------------ロールのコレクションを戻す
	 @Override
	 public Collection<?extends GrantedAuthority> getAuthorities(){
		 return authorities;
		 }
	 
	 //-------------------------アカウントが期限切れかどうかを確認する
	 @Override
	 public boolean isAccountNonExpired() {
		 return true;
		 
	 }
	 
	 //--------------------------ユーザーがロックされているかを確認する
	 @Override
	 public boolean isAccountNonLocked() {
		 return true;
		 
	 }
	 
	 //--------------------------ユーザーが有効か確認する
	 @Override
	 public boolean isEnabled() {
		 return user.getEnabled();
	 }
	 
	 @Override
	 public boolean isCredentialsNonExpired() {
	     return true; // または、必要に応じて適切なロジックを実装
	 }
	 
	 // ユーザータイプに基づいてロールを設定
	    public String getUserType() {
	        return user.getUserType(); // userType をそのまま戻す
	    }


	 }
 