package com.example.nagoyameshi.entity;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import lombok.Data;

@Entity
@Table(name="users")
@Data
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Integer id;
    
    @Column(name="name")
    private String name;
    
    @Column(name="email")
    private String email;
    
    @Column(name="phone_number")
    private String phoneNumber;
    
    @Column(name="password")
    private String password;
    
    @Column(name="user_type")
    private String userType;
    
    @Column(name = "enabled")
    private Boolean enabled;

    // サブスクリプションID（Stripe用）
    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;
    
    // 顧客ID（Stripe用）
    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name="created_at", insertable=false, updatable=false)
    @CreationTimestamp
    private Timestamp createdAt;
    
    @Column(name="updated_at", insertable=false, updatable=false)
    @UpdateTimestamp
    private Timestamp updatedAt;
    
    // ユーザーのロールを返すメソッド
    public String getRole() {
        return "ROLE_" + userType;
    }
    
    // userType の getter, setter
    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    // 権限を返すメソッド
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(getRole()));
    }
}
