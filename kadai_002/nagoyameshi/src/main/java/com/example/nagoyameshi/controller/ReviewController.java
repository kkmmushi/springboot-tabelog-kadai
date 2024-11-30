package com.example.nagoyameshi.controller;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;  // UserDetailsImplをインポート
import com.example.nagoyameshi.service.ReviewService;
import com.example.nagoyameshi.service.ShopService;

@Controller
public class ReviewController {

    @Autowired
    private ShopService shopService;  // 店舗情報を取得するサービス
    
    @Autowired
    private ShopRepository shopRepository;

    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewService reviewService;  // レビュー投稿を行うサービス
    
    @Autowired
    private CategoryRepository categoryRepository;

    // レビュー投稿フォームから確認ページに遷移
    @PostMapping("/submit-review")
    public String submitReview(@RequestParam Integer shopId, 
                               @RequestParam Integer rating, 
                               @RequestParam String reviewContent, 
                               Model model) {

        // セキュリティコンテキストから現在のユーザー情報を取得
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();  // UserDetailsImpl からユーザー名を取得

        // 店舗情報を取得
        Shop shop = shopService.getShopById(shopId);

        // レビュー確認ページに必要な情報をModelに追加
        model.addAttribute("user", userDetails);  // UserDetailsImpl インスタンスを追加
        model.addAttribute("username", username);
        model.addAttribute("shop", shop);
        model.addAttribute("rating", rating);
        model.addAttribute("reviewContent", reviewContent);

        // レビュー確認ページに遷移
        return "review-confirmation";  // 確認ページのテンプレート名
    }
    
    // レビューの最終投稿処理
    @PostMapping("/submit-review-final")
    public String submitFinalReview(@RequestParam Integer shopId,
                                    @RequestParam int rating,
                                    @RequestParam String reviewContent,
                                    @AuthenticationPrincipal UserDetails userDetails,  // Spring Securityでユーザー情報を取得
                                    Model model, RedirectAttributes redirectAttributes) {
    	
    	
    	// ユーザー情報を取得 (Spring Securityから取得)
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email);
        
        System.out.println("レビューユーザー："+user);
        System.out.println("ユーザーID：" + user.getId()); 
        
        // shopIdを使ってShopを取得
        Shop shop = shopService.getShopById(shopId);  // ShopServiceからショップ情報を取得
        // レビューを保存
        Review review = new Review();
        review.setShopId(shop);  // Shopオブジェクトをセット
        review.setRate(rating);
        review.setReviewContent(reviewContent);
        review.setUserId(user);  
        reviewService.saveReview(review);  // レビュー保存用サービス

        // 成功メッセージ
        redirectAttributes.addFlashAttribute("successMessage", "レビューが投稿されました。");

        // 店舗詳細ページにリダイレクト
        return "redirect:/shop/" + shopId;  // 店舗詳細ページへのリダイレクト
    }
    
    @GetMapping("/reviewsAll")
    public String getUserReviews(Model model, 
                                  @AuthenticationPrincipal UserDetails userDetails) {
        // ユーザー情報を取得 (Spring Securityから取得)
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email);

        // ユーザーに基づいてレビュー情報を取得
        List<Review> reviews = reviewService.findByUser(user);

        // レビュー内容をモデルに追加
        model.addAttribute("reviews", reviews);

        // フラッシュメッセージをモデルに追加
        if (model.containsAttribute("successMessage")) {
            model.addAttribute("successMessage", model.getAttribute("successMessage"));
        }
        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", model.getAttribute("errorMessage"));
        }

        // ログ出力（デバッグ用）
        System.out.println("レビューユーザー：" + user);
        System.out.println("レビュー内容" + reviews);

        return "review-list"; // review-list.html テンプレートを返す
    }

    
    
    
    @PostMapping("/reviews/delete/{reviewNo}")
    public String deleteReview(@PathVariable Integer reviewNo, RedirectAttributes redirectAttributes) {
        try {
            // レビュー削除の処理をサービスに依頼
            reviewService.deleteReview(reviewNo);
            // フラッシュメッセージを設定
            redirectAttributes.addFlashAttribute("successMessage", "レビューが削除されました。");
        } catch (Exception e) {
            // エラーメッセージを設定
            System.out.println("レビュー削除エラー内容：" + e);
            redirectAttributes.addFlashAttribute("errorMessage", "削除に失敗しました。");
        }
        // レビュー一覧ページにリダイレクト
        return "redirect:/reviewsAll";
    }

    
    // レビュー編集ページを表示
    @GetMapping("/reviews/edit/{reviewNo}")
    public String editReview(@PathVariable Integer reviewNo, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // レビューを取得
        Review review = reviewService.findByNo(reviewNo);
        if (review == null) {
            model.addAttribute("errorMessage", "レビューが見つかりませんでした。");
            return "redirect:/reviewsAll";
        }

        // レビューが現在のユーザーに紐づいているかチェック
        if (!review.getUserId().getEmail().equals(userDetails.getUsername())) {
            model.addAttribute("errorMessage", "他のユーザーのレビューは編集できません。");
            return "redirect:/reviewsAll";
        }

        // 店舗情報とカテゴリー情報をモデルに追加
        model.addAttribute("review", review);
        model.addAttribute("categoryMap", categoryRepository.findCategoryMap());

        return "review-edit"; // review-edit.html に遷移
    }

    // レビューを更新
    @PostMapping("/reviews/edit/{reviewNo}")
    public String updateReview(@PathVariable Integer reviewNo, @RequestParam Integer rating, @RequestParam String reviewContent, RedirectAttributes redirectAttributes) {
        try {
            // レビューを取得
            Review review = reviewService.findByNo(reviewNo);
            if (review == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "レビューが見つかりませんでした。");
                return "redirect:/reviewsAll";
            }

            // レビューの内容を更新
            review.setRate(rating);
            review.setReviewContent(reviewContent);
            review.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            // 更新を保存
            reviewService.saveReview(review);
            redirectAttributes.addFlashAttribute("successMessage", "レビューが更新されました。");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "レビューの更新に失敗しました。");
        }

        return "redirect:/reviewsAll";
    } 
    
    
    
}
