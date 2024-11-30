package com.example.nagoyameshi.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.AdminService;

@Controller
public class AdminController {
	
	

    @Autowired
    private UserRepository userRepository; // AdminServiceを注入

    @Autowired
    private AdminService adminService; // AdminServiceを注入

    // ログインページ表示
    @GetMapping("/admin/login")
    public String showLoginPage() {
        return "admin/admin-login";  
    }

    @PostMapping("/admin/login")
    public String login(@RequestParam("email") String email, 
                        @RequestParam("password") String password, 
                        Model model, 
                        HttpSession session, 
                        HttpServletRequest request) {

        // AdminServiceで認証
        System.out.println("Login attempt with email: " + email);
        boolean isAuthenticated = adminService.authenticateAdmin(email, password);

        if (!isAuthenticated) {
            System.out.println("Authentication failed for email: " + email);
            model.addAttribute("error", "認証に失敗しました。メールアドレスまたはパスワードを確認してください。");
            return "admin/admin-login";  // 認証失敗時、再度ログインページを表示
        }

        // 認証成功時、セッションに管理者情報を保存
        System.out.println("Authentication successful for email: " + email);
        session.setAttribute("adminEmail", email);  // セッションにadminEmailを保存
        System.out.println("adminEmail set in session: " + email);

        // 自動的にROLE_ADMINをセッションに追加
        session.setAttribute("adminRole", "ROLE_ADMIN");  // ここで自動的にROLE_ADMINを設定
        System.out.println("adminRole set in session: ROLE_ADMIN");
        
        
        session.setAttribute("isAdminAuthenticated", true); 

        // Spring Securityのセキュリティコンテキストに認証情報を保存
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(email, password, 
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));  // 役割情報も設定
        System.out.println("Authentication token created for email: " + email);

        // セキュリティコンテキストに認証情報を設定
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("SecurityContext authentication set for email: " + email);

        // セッションの状態を確認
        System.out.println("Session ID after login: " + session.getId());
        System.out.println("Session contains adminEmail: " + (session.getAttribute("adminEmail") != null));

        // 認証後、リダイレクト先に遷移
        System.out.println("Redirecting to /admin/base after successful login.");
        return "redirect:/admin/base";  // ログイン成功後、管理者ダッシュボードにリダイレクト
    }

    // 管理者ダッシュボードページ
    @GetMapping("/admin/base")
    public String adminBase(HttpSession session, Model model) {

        // セッションの状態をログに記録
        System.out.println("Session ID when accessing /admin/base: " + session.getId());
        System.out.println("Session contains adminEmail: " + (session.getAttribute("adminEmail") != null));

        // セキュリティコンテキストから認証情報を取得
        UsernamePasswordAuthenticationToken authentication = 
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            System.out.println("Authentication details: " + authentication.getName()); // emailが設定されていることを確認
            System.out.println("Authorities: " + authentication.getAuthorities()); // ROLE_ADMINが設定されているか確認
        } else {
            System.out.println("Authentication is not set in SecurityContext.");
        }

        // セッションからadminEmailを取得
        String adminEmail = (String) session.getAttribute("adminEmail");

        if (adminEmail == null) {
            System.out.println("Admin email not found in session. Redirecting to login.");
            return "redirect:/admin/login";  // ログインされていない場合、ログインページにリダイレクト
        }

        model.addAttribute("adminEmail", adminEmail);
        return "admin/admin-base";  // ダッシュボードページに遷移
    }

    // ログアウト処理
    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        // セッションからadminEmailを削除
        session.removeAttribute("adminEmail");
        System.out.println("adminEmail removed from session during logout.");
        return "redirect:/admin/login";  // ログインページにリダイレクト
    }
    
    
    //ーーーーーーーーーーーーーーーーーーーーーーーー以下ダッシュボードから下層
    


    
    @GetMapping("/admin/user")
    public String listUsers(@RequestParam(value = "memberName", required = false) String memberName, Model model) {
        List<User> users;
        
        if (memberName != null && !memberName.isEmpty()) {
            // メールアドレスで検索する
            users = userRepository.findByEmailContaining(memberName);
        } else {
            // 検索条件がない場合は全てのユーザーを表示
            users = userRepository.findAll();
        }

        model.addAttribute("members", users);
        model.addAttribute("memberName", memberName);
        return "admin/admin-user"; // 表示するテンプレート名
    }

    
}
