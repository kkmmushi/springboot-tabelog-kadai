package com.example.nagoyameshi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ユーザー用の認証設定
        http
            .authorizeHttpRequests(requests -> requests
            	.requestMatchers("/shop-list/**").permitAll()
                .requestMatchers("/admin/admin-login","/admin/login","/login","/auth/passmissing", "/auth/passmissing/**", "/passmissing", "/passmissing/**","/auth/passmissing/**","/passmissing/**","/auth/**","/passmissing/**","/css/**", "/images/**",  "/storage/**", "/", "/auth/signup", "/signup", "/auth/verify", "/signup/**","/passmissing/confirm","index","/shop-detail","/shop/**","/shop-detail/**","shop-detail/**","shop/**").permitAll() 
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()   
              //.anyRequest().permitAll()
                
            )
            .formLogin(form -> form
                .loginPage("/login")                    // ログインページのURL
                .loginProcessingUrl("/login")           // ログインフォームの送信先URL
                .defaultSuccessUrl("/")           // ログイン成功後のリダイレクト先
                .failureUrl("/login?error")       // ログイン失敗時のリダイレクト先
                .permitAll()                            // ログインページは誰でもアクセス可能
            )
            
           
            .logout(logout -> logout
                .logoutUrl("/logout")                   // ログアウトのURL
                .logoutSuccessUrl("/login?logout=true") // ログアウト成功後のリダイレクト先
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/stripe/webhook", "/charge","/admin/**", "/updateCard","/userInfo"));
        


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
