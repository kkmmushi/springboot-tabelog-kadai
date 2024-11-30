package com.example.nagoyameshi.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nagoyameshi.entity.Like;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.LikeRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;

@Controller
public class FavoriteController {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    // お気に入り追加または削除処理
    @PostMapping("/add-favorite")
    public String addFavorite(@RequestParam Integer shopId, Model model) {
        // ユーザー情報を取得（UserDetailsImplを使ってユーザーを取得）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser(); // UserDetailsImplからUserエンティティを取得

        // Shop情報を取得
        Optional<Shop> optionalShop = shopRepository.findById(shopId);

        if (!optionalShop.isPresent()) {
            model.addAttribute("error", "指定された店舗が見つかりません");
            return "redirect:/shop-list";  // 店舗一覧にリダイレクト
        }

        Shop shop = optionalShop.get();

        // ユーザーと店舗に関連するお気に入りデータを検索
        Optional<Like> existingLike = likeRepository.findByShopIdAndUserId(shop, user);

        if (existingLike.isPresent()) {
            // 既にお気に入りに登録されている場合、削除
            likeRepository.delete(existingLike.get());
            model.addAttribute("message", "お気に入りから削除しました！");
        } else {
            // まだお気に入りに登録されていない場合、新たに追加
            Like like = new Like();
            like.setShopId(shop);
            like.setUserId(user);
            like.setIsLike(1); // お気に入りにする

            // お気に入りをデータベースに保存
            likeRepository.save(like);
            model.addAttribute("message", "お気に入りに追加しました！");
        }

        // リダイレクト先にショップ詳細ページを指定
        return "redirect:/shop/" + shop.getShopId();
    }
}
