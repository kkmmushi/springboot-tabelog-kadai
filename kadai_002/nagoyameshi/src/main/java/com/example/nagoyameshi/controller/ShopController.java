package com.example.nagoyameshi.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.LikeRepository;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.ShopService;

@Controller
public class ShopController {

    private final ShopService shopService;
    private final CategoryService categoryService;

    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserRepository userRepository;

    

    public ShopController(ShopService shopService, CategoryService categoryService) {
        this.shopService = shopService;
        this.categoryService = categoryService;
    }

    @GetMapping("/shop-list")
    public String getShopList(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "categoryNo", required = false) Integer categoryNo,
            @RequestParam(value = "search", required = false) String search,
            Model model) {
    	
    	
    	
        // 認証確認
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) principal;
                User user = userDetails.getUser();  // Userオブジェクトを取得
                model.addAttribute("user", user);   // モデルにユーザー情報を追加
            } else {
                // principalがUserDetailsImpl型ではない場合
                System.out.println("principalがUserDetailsImpl型ではありません");
            }
        } else {
            // 未認証ユーザーの場合
            System.out.println("未認証のユーザーです");
            model.addAttribute("authenticated", false);  // 認証されていないユーザーのフラグを追加
        }

    	

        
        Pageable pageable = PageRequest.of(page, 8);  // 1ページ8件表示
        Page<Shop> shopPage;

        // カテゴリと店舗名の両方で検索
        if (categoryNo != null && categoryNo != 0 && search != null && !search.isEmpty()) {
            // カテゴリと店舗名での絞り込み
            shopPage = shopService.getShopsByCategoryAndSearch(categoryNo, search, pageable);
        } else if (categoryNo != null && categoryNo != 0) {
            // カテゴリでの絞り込み
            shopPage = shopService.getShopsByCategory(categoryNo, pageable);
        } else if (search != null && !search.isEmpty()) {
            // 店舗名での絞り込み（部分一致）
            shopPage = shopService.searchShops(search, pageable);
        } else {
            // 何も指定されていない場合、全ての店舗を表示
            shopPage = shopService.getAllShops(pageable);
        }

        // 店舗名での検索結果がない場合
        if (search != null && !search.isEmpty() && shopPage.getTotalElements() == 0) {
            model.addAttribute("noResultsMessage", "該当する店舗がありません");
        }

        // カテゴリ一覧を取得
        List<Category> categories = categoryService.getAllCategories();
        Map<Integer, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getCategoryNo, Category::getCategoryName)); // getCategoryNo() を使用

        model.addAttribute("categoryMap", categoryMap);
        model.addAttribute("shops", shopPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", shopPage.getTotalPages());
        model.addAttribute("totalItems", shopPage.getTotalElements());

        // 検索フォームに現在のカテゴリ番号と検索文字列を保持
        model.addAttribute("selectedCategoryNo", categoryNo);
        model.addAttribute("searchText", search);
       

        

        return "shop-list";
    }
    
    
    
    
    

    @GetMapping("/shop/{shopId}")
    public String showShopDetail(@PathVariable Integer shopId, Model model) {
        // 店舗情報を取得
        Shop shop = shopService.getShopById(shopId);
        model.addAttribute("shop", shop);
        model.addAttribute("categoryMap", shopService.getCategoryMap()); // カテゴリのマップを追加

        // ユーザーがその店舗をお気に入りにしているか確認
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 認証されている場合のみ処理
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            // principalがUserDetailsImpl型であることを確認
            if (principal instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) principal;
                User user = userDetails.getUser();  // Userオブジェクトを取得
                model.addAttribute("user", user);

                // ユーザーがその店舗をお気に入りにしているかをチェック
                boolean isFavorite = likeRepository.findByShopIdAndUserId(shop, user).isPresent();
                model.addAttribute("isFavorite", isFavorite);  // お気に入り状態をテンプレートに渡す
            } else {
                // principalがUserDetailsImpl型でない場合の処理（エラーハンドリングなど）
                // 必要に応じてエラーメッセージなどを追加
                model.addAttribute("isFavorite", false);  // 仮にfalseをセット
            }
        } else {
            // 認証されていない場合
            model.addAttribute("isFavorite", false);  // 仮にfalseをセット
        }
        
        
        // 店舗の全レビューを取得
        List<Review> reviews = reviewRepository.findByShopIdOrderByCreatedAtDesc(shop);
        model.addAttribute("reviews", reviews);
        
     // ユーザー情報をマップで取得
        Map<Integer, User> userMap = new HashMap<>();
        for (Review review : reviews) {
            User user = review.getUserId();  // userId は User 型なので、そのまま取得
            if (user != null) {
                userMap.put(user.getId(), user);  // User の ID をキーとして格納
            }
        }


        // 4件までレビューを表示
        model.addAttribute("topReviews", reviews.stream().limit(4).collect(Collectors.toList()));
        model.addAttribute("reviews", reviews);
        model.addAttribute("userMap", userMap);  // ユーザー情報をマップで渡す

        return "shop-detail";  // 店舗詳細ページのテンプレート
    }

    @GetMapping("/likesAll")
    public String showFavoriteShops(Model model,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "8") int size) {

        // Spring SecurityのContextからユーザー情報を取得
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 認証情報が存在し、かつ、そのPrincipalがUserDetailsImplである場合
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();  // UserDetailsとしてのUserオブジェクトを取得
            User user = userDetails.getUser();  // Userオブジェクトを取得

            Pageable pageable = PageRequest.of(page, size);  // PageRequestでPageableを作成
            List<Shop> favoriteShops = shopService.getFavoriteShopsForUser(user, pageable);

            int totalFavoriteShops = shopService.getTotalFavoriteShopsForUser(user);
            int totalPages = (int) Math.ceil((double) totalFavoriteShops / size);

            model.addAttribute("favoriteShops", favoriteShops);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("categoryMap", shopService.getCategoryMap());

            return "like-list";  // like-listビューに遷移
        }

        // 認証情報が無い場合はログイン画面へリダイレクト
        return "redirect:/login";
    }
    
    
    @GetMapping("/reviews/{shopId}")
    public String showAllReviews(@PathVariable Integer shopId, Model model) {
        // 店舗情報を取得
        Shop shop = shopService.getShopById(shopId);
        model.addAttribute("shop", shop);

        // すべてのレビューを取得
        List<Review> reviews = reviewRepository.findByShopIdOrderByCreatedAtDesc(shop);
        model.addAttribute("reviews", reviews);

        return "reviews-detail";  // レビュー一覧ページのテンプレート
    }
    
    
    
}
