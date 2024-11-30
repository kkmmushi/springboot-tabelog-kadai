package com.example.nagoyameshi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.service.StripeService;
import com.example.nagoyameshi.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;

@Controller
public class PaymentController {

    @Autowired
    private UserService userService;
    private UserRepository userRepository;

    @Autowired
    private StripeService stripeService;

    public PaymentController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/charge")
    public String charge(@RequestParam("stripeToken") String token, 
                         @RequestParam("email") String email, 
                         @RequestParam("userId") Integer userId,
                         RedirectAttributes redirectAttributes) {
    	
 
    	 System.out.println("取得したユーザー情報: " + email+userId);
    	 
        try {
            // メールアドレスでユーザー情報を取得
           User user = userRepository.findByEmail(email);
 
            // ユーザー情報をログに出力
            System.out.println("取得したユーザー情報: " + user);
                       
            // ユーザーがnullかどうかをチェック
            if (user == null) {
                System.out.println("ユーザー情報が取得できませんでした。email: " + email);
                redirectAttributes.addFlashAttribute("errorMessage", "ユーザー情報の取得に失敗しました。");
                return "redirect:/userInfo";  // エラーページへリダイレクト
            }

            // Stripeの支払い方法IDを取得（tokenがクライアントから渡される場合もあります）
            String paymentMethodId = token;  // ここでは仮にstripeTokenを使用

            // サブスクリプションを作成
            System.out.println("createSubscriptionメソッドを呼び出します");
            Subscription subscription = stripeService.createSubscription(user, paymentMethodId);

         // メールアドレスを使ってサブスクリプションIDをユーザー情報に保存
            userService.saveSubscriptionId(email, subscription.getId());  // userIdではなくemailを使用

            // メールアドレスを使ってユーザーを有料会員にアップグレード
            userService.upgradeToPaidUser(email);  // userIdではなくemailを使用

            // 成功メッセージをリダイレクト属性にセット
            redirectAttributes.addFlashAttribute("successMessage", "支払いが成功し、有料会員に登録されました！");
            return "redirect:/userInfo"; // ユーザー情報ページにリダイレクト

        } catch (StripeException e) {
            // 支払いに失敗した場合、エラーメッセージをリダイレクト属性にセット
            e.printStackTrace(); // エラーログを出力
           
            redirectAttributes.addFlashAttribute("errorMessage", "支払いに失敗しました。再度お試しください。");
            return "redirect:/userInfo"; // ユーザー情報ページにリダイレクト
        }
    }
}
