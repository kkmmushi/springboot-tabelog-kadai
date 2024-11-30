package com.example.nagoyameshi.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.StripeService;
import com.example.nagoyameshi.service.UserService;

@Controller
public class SubscriptionController {

    @Autowired
    private UserService userService;  // ユーザー情報を取得するサービス（必要に応じて実装）
    
    @Autowired
    private StripeService stripeService;  // Stripeサービス
    
    
    @Value("${stripe.public.key}")
    private String stripePublicKey;

    

    // 有料会員登録ページへの遷移
    @GetMapping("/subscribe")
    public String showSubscriptionPage(Model model) {
        // 現在ログインしているユーザーを取得
        User currentUser = userService.getCurrentUser();

        // ユーザーが無料会員であることを確認
        if (currentUser == null) {
            model.addAttribute("errorMessage", "ログインしてください。");
            return "redirect:/login";  // ログインページにリダイレクト
        }

        // 文字列を直接比較
        if (!"FREE_USER".equals(currentUser.getUserType())) {
            // 無料会員でない場合、エラーメッセージを表示して会員情報ページにリダイレクト
            model.addAttribute("errorMessage", "すでに有料会員です。");
            return "redirect:/userInfo";  // 会員情報ページにリダイレクト
        }

        // 無料会員の場合、サブスクリプションページを表示
        model.addAttribute("user", currentUser);  // 現在のユーザー情報をテンプレートに渡す
        model.addAttribute("stripePublicKey", stripePublicKey);  // モデルに公開鍵を渡す
        return "subscribe";  // 有料会員登録ページ（subscribe.html）
    }

    // 解約確認ページへの遷移
    @GetMapping("/unsubscribe")
    public String unsubscribePage(Model model) {
        // ユーザー情報をモデルに追加
        model.addAttribute("user", userService.getCurrentUser());
        return "unsubscribe"; // 解約確認ページ
    }

    // 解約処理の確認
    @Controller
    public class SubscriptionEndController {

        @Autowired
        private UserService userService;

        @Autowired
        private StripeService stripeService;

        // 解約処理の確認
        @GetMapping("/unsubscribe/confirm")
        public String confirmUnsubscribe(Model model) {
            try {
                // 現在のユーザーIDを取得
                User user = userService.getCurrentUser();

                // ユーザーが取得できなかった場合のエラーハンドリング
                if (user == null) {
                    model.addAttribute("errorMessage", "ユーザー情報が見つかりませんでした。");
                    return "error"; // エラーページへ遷移
                }

                // StripeのサブスクリプションIDを取得して解約処理
                boolean success = stripeService.cancelSubscription(user.getStripeSubscriptionId());

                if (success) {
                    // 解約成功時
                    user.setUserType("FREE_USER");  // 文字列で"FREE_USER"を設定
                    userService.save(user);  // ユーザー情報を更新
                    model.addAttribute("successMessage", "有料会員の解約が完了しました。");
                } else {
                    // 解約失敗時
                    model.addAttribute("errorMessage", "解約処理に失敗しました。再度お試しください。");
                }

                model.addAttribute("user", user);  // user情報をテンプレートに渡す

            } catch (Exception e) {
                model.addAttribute("errorMessage", "解約処理中にエラーが発生しました。");
                e.printStackTrace();
            }

            return "user/userInfo"; // 会員情報ページに遷移
        }
    }
    
    
 // 有料会員情報編集ページへの遷移
    @GetMapping("/editSubscription")
    public String editSubscriptionPage(Model model) {
        // 現在ログインしているユーザーを取得
        User currentUser = userService.getCurrentUser();

        // ユーザーが有料会員であることを確認
        if (currentUser == null || !"PAID_USER".equals(currentUser.getUserType())) {
            model.addAttribute("errorMessage", "有料会員としてログインしてください。");
            return "redirect:/login";  // ログインページにリダイレクト
        }

        // 現在のユーザー情報をテンプレートに渡す
        model.addAttribute("user", currentUser);  
        model.addAttribute("stripePublicKey", stripePublicKey);  // モデルに公開鍵を渡す
        return "editSubscription";  // クレジットカード情報編集ページ（editSubscription.html）
    }

    // クレジットカード情報更新処理
    @PostMapping("/updateCard")
    public String updateCard(@RequestParam("stripeToken") String stripeToken, Model model,RedirectAttributes redirectAttributes,HttpSession session) {
    	System.out.println("Received stripeToken: " + stripeToken);  // トークンのログを表示
    	
        if (stripeToken == null || stripeToken.isEmpty()) {
            System.err.println("Received empty stripeToken");
        } else {
            System.out.println("Received stripeToken: " + stripeToken);
        }
    	
    	try {
    		
    		// 顧客IDとサブスクリプションIDをログに表示
            System.out.println("Received token: " + stripeToken);

            // 現在のユーザー情報を取得
            User currentUser = userService.getCurrentUser();
            System.out.println("Updated customer: " + currentUser);
            
            if (currentUser == null) {
                model.addAttribute("errorMessage", "ユーザーが見つかりません。");
                return "redirect:/login";  // ログインページにリダイレクト
            }

            // Stripeのクレジットカード情報を更新
            boolean success = stripeService.updatePaymentMethod(currentUser, stripeToken);
            
            if (success) {
            	 session.setAttribute("successMessage", "クレジットカード情報が更新されました。");
            } else {
            	 session.setAttribute("errorMessage", "クレジットカード情報の更新に失敗しました。");
            }
            System.out.println("Redirecting to /userInfo");
            return "redirect:/userInfo";  // ユーザー情報ページにリダイレクト
        } catch (Exception e) {
        	 session.setAttribute("errorMessage", "エラーが発生しました。再度お試しください。");
            System.err.println("Stripe error: " + e.getMessage());
            return "redirect:/userInfo"; // ユーザー情報ページにリダイレクト
        }
    }

}
