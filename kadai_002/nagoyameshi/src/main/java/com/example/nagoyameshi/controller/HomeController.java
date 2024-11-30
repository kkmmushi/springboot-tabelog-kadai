package com.example.nagoyameshi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;

@Controller
public class HomeController {
    
    
    
    // ユーザー情報とユーザータイプをモデルに追加するメソッド
	private void addUserToModel2(Model model) {
	    // Spring Security の SecurityContext から認証情報を取得
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

	    // 認証情報が取得できているか確認
	    System.out.println("Authentication: " + authentication);  // authentication オブジェクトの中身を表示

	    if (authentication == null) {
	        System.out.println("No authentication found.");
	        return;
	    }

	    // 認証情報の中身をチェック（UserDetailsImpl型かどうか確認）
	    Object principal = authentication.getPrincipal();
	    System.out.println("Principal: " + principal);  // 認証されたユーザー（または匿名ユーザー）

	    if (!(principal instanceof UserDetailsImpl)) {
	        System.out.println("The principal is not of type UserDetailsImpl.");
	        return;
	    }

	    // 認証されたユーザー情報を取得（UserDetailsImpl）
	    UserDetailsImpl userDetails = (UserDetailsImpl) principal;


	    // モデルにユーザー情報とユーザータイプを追加
	    model.addAttribute("user", userDetails);

	}
	
	//-----------------------!!!!!!!!!!!!11試しに!!!!!!!!!!!----------------------------
    // ユーザー情報を取得して渡す
    private void addUserToModel(HttpSession session, Model model) {
        Object securityContext = session.getAttribute("SPRING_SECURITY_CONTEXT");
        if (securityContext != null) {
        	
        	UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        	User user = userDetails.getUser();

            model.addAttribute("user", user);  // ユーザー情報をmodelに追加
        }
    }
  //-----------------------!!!!!!!!!!!!11試しに!!!!!!!!!!!----------------------------


    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        // ユーザー情報をモデルに追加
        addUserToModel(session, model);

        // ユーザー情報が存在する場合、ユーザータイプをコンソールに表示
        if (model.getAttribute("user") != null) {
            User user = (User) model.getAttribute("user");
            System.out.println("User Type: " + user.getUserType());  // サーバーのコンソールにユーザータイプを表示
            model.addAttribute("userType", user.getUserType());  // ユーザータイプをフロントエンドへ渡す
        } else {
            System.out.println("No user logged in.");  // サーバーのコンソールにユーザーがログインしていないことを表示
            model.addAttribute("userType", "No user logged in");  // ログインしていない場合のメッセージをフロントエンドへ渡す
        }

        return "index";  // index.htmlを表示
    
    }

    @GetMapping("/index")
    public String index2(HttpSession session, Model model) {
        addUserToModel(session, model);  // ユーザー情報をモデルに追加
        return "index";  // index.htmlを表示
    }
    
    @GetMapping("/error")
    public String handleError(HttpServletRequest request) {
        // エラーメッセージなどを表示する処理
        return "error";  // error.html を表示
    }
    
}
