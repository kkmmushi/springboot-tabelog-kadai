package com.example.nagoyameshi.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.repository.VerificationTokenRepository;

@Service
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;    
    private final VerificationTokenRepository verificationTokenRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, VerificationTokenRepository verificationTokenRepository) {        
        this.userRepository = userRepository;       
        this.passwordEncoder = passwordEncoder;
        this.verificationTokenRepository = verificationTokenRepository;
    }    

    // パスワードリセット処理
    public boolean resetPassword(String token, String newPassword) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        
        if (verificationToken != null ) {
            User user = verificationToken.getUser();
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            userRepository.save(user);
            verificationTokenRepository.delete(verificationToken);
            return true;
        }
        
        return false;
    }

    // ユーザー作成処理
    @Transactional
    public User create(SignupForm signupForm) {
        User user = new User();
        user.setName(signupForm.getName());
        user.setPhoneNumber(signupForm.getPhoneNumber());
        user.setEmail(signupForm.getEmail());
        user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
        user.setUserType("FREE_USER");
        user.setEnabled(false);
        return userRepository.save(user);
    }

    // メールアドレスが登録済みかどうかをチェックする
    public boolean isEmailRegistered(String email) {
        User user = userRepository.findByEmail(email);  
        return user != null;
    }

    // パスワード確認
    public boolean isSamePassword(String password, String passwordConfirmation) {
        return password.equals(passwordConfirmation);
    }

    // ユーザーを有効にする
    @Transactional
    public void enableUser(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    // メールアドレスでユーザーを取得
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 現在の認証ユーザーを取得
    public User getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            
            if (principal instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) principal;

                
                User user = userRepository.findByEmail(userDetails.getUsername());
               
                
                if (user != null) {
                    System.out.println("取得したユーザー: {}" + user);
                    return user;
                } else {
                    System.out.println("指定されたメールアドレスに該当するユーザーが見つかりませんでした。email: {}" + userDetails.getUsername());
                    return null;
                }
            } else {
                System.out.println("認証情報がUserDetails型ではありません。principal: {}" + principal);
                return null;
            }
        } catch (Exception e) {
            System.out.println("ユーザー情報の取得中にエラーが発生しました: " + e);
            return null;
        }
    }

    // ユーザー情報更新（名前と電話番号のみ更新）
    @Transactional
    public User updateUser(User currentUser, String newName, String newPhoneNumber) {
        if (newName != null && !newName.equals(currentUser.getName())) {
            currentUser.setName(newName);
        }
        if (newPhoneNumber != null && !newPhoneNumber.equals(currentUser.getPhoneNumber())) {
            currentUser.setPhoneNumber(newPhoneNumber);
        }

        return userRepository.save(currentUser);
    }
    
    // メールアドレスを更新するメソッド
    @Transactional
    public User updateUserEmail(User currentUser, String newEmail) {
        if (newEmail != null && !newEmail.equals(currentUser.getEmail())) {
            currentUser.setEmail(newEmail); 
        }

        return userRepository.save(currentUser); 
    }

    /**
     * 支払い完了後にユーザーを有料会員にアップグレードする
     * @param email 支払いを完了したユーザーのメールアドレス
     */
    @Transactional
    public void upgradeToPaidUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setUserType("PAID_USER");
            userRepository.save(user);
        } else {
            throw new RuntimeException("ユーザーが見つかりません: " + email);
        }
    }

    // サブスクリプションIDをユーザーに保存するメソッド
    @Transactional
    public void saveSubscriptionId(String email, String subscriptionId) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setStripeSubscriptionId(subscriptionId);
            userRepository.save(user);
        } else {
            throw new RuntimeException("ユーザーが見つかりません: " + email);
        }
    }
    
    @Transactional
    public void downgradeToFreeUser(User user) {
        user.setUserType("FREE_USER");
        userRepository.save(user);  
    }
    
    // ユーザー情報を保存するメソッド
    @Transactional
    public User save(User user) {
        return userRepository.save(user);
    }
}
