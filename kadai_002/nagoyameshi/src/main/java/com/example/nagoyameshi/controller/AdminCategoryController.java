package com.example.nagoyameshi.controller;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.LikeRepository;
import com.example.nagoyameshi.repository.ReservationRepository;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.ShopRepository;
import com.example.nagoyameshi.service.CategoryService;

@Controller
@RequestMapping("/admin/category")
public class AdminCategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;
    
    
    @Autowired
    private ShopRepository shopRepository;
    
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private ReservationRepository reservationRepository;   
    
    
    
    @GetMapping
    public String getCategoryList(@RequestParam(value = "categoryName", required = false) String categoryName, Model model) {
        List<Category> categories;

        if (categoryName != null && !categoryName.isEmpty()) {
            categories = categoryService.findCategoriesByName(categoryName);
        } else {
            categories = categoryService.findAllCategories();
        }

        model.addAttribute("categories", categories);
        model.addAttribute("categoryName", categoryName); // 検索条件をフォームに反映
        return "admin/admin-category";  // Thymeleafテンプレートのパス
    }

    @Transactional
    @PostMapping("/delete/{no}")
    public String deleteCategory(@PathVariable Integer no) {
        // 1. カテゴリに関連する店舗を取得
        Category category = categoryRepository.findById(no).orElseThrow(() -> new RuntimeException("Category not found"));

        List<Shop> shops = shopRepository.findByCategoryNo(no);  // カテゴリIDに関連する店舗を取得
        
        for (Shop shop : shops) {
            // 2. 店舗に関連するlikesを削除
            likeRepository.deleteByShopId(shop);
            
            // 3. 店舗に関連する予約データを削除
            List<Reservation> reservations = reservationRepository.findByShopId(shop);
            reservationRepository.deleteAll(reservations);

            // 4. 店舗に関連するレビューを削除
            List<Review> reviews = reviewRepository.findByShopId(shop);
            reviewRepository.deleteAll(reviews);

            // 5. 最後に店舗を削除
            shopRepository.delete(shop);
        }

        // 6. カテゴリを削除
        categoryRepository.deleteById(no);

        // 7. 削除後、カテゴリ管理画面にリダイレクト
        return "redirect:/admin/category";
    }
    
    
    // 新規カテゴリー登録ページを表示
    @GetMapping("/new")
    public String showCategoryForm(Model model) {
        // カテゴリー作成フォームのために何も追加する必要がない場合
        return "admin/admin-category-add"; // 新規作成フォームのテンプレート名
    }

    // カテゴリーの保存処理
    @PostMapping("/create")
    public String createCategory(@RequestParam("categoryName") String categoryName,
    		                     RedirectAttributes redirectAttributes,
                                 Model model) {
        try {
            // カテゴリーを保存する処理（仮のメソッド）
            categoryService.saveCategory(categoryName);
            model.addAttribute("successMessage", "カテゴリーが正常に追加されました");
            redirectAttributes.addFlashAttribute("successMessage", "カテゴリ登録が完了しました");
            return "redirect:/admin/category"; // 一覧ページにリダイレクト
        } catch (Exception e) {
            model.addAttribute("errorMessage", "カテゴリーの追加に失敗しました");
            return "categoryForm"; // 新規作成フォームに戻る
        }
    }
    
    
    // カテゴリ編集ページ（カテゴリ情報を表示）
    @GetMapping("/edit/{categoryNo}")
    public String editCategory(@PathVariable Integer categoryNo, Model model) {
        Category category = categoryService.getCategoryByNo(categoryNo);
        if (category == null) {
            model.addAttribute("errorMessage", "カテゴリが見つかりませんでした。");
            return "redirect:/admin/category";
        }
        model.addAttribute("category", category);
        return "admin/admin-category-edit";
    }

    // カテゴリ更新処理
    @PostMapping("/update")
    public String updateCategory(@ModelAttribute Category category, Model model) {
        try {
            categoryService.updateCategory(category);
            model.addAttribute("successMessage", "カテゴリが更新されました。");
            return "redirect:/admin/category";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "更新に失敗しました。");
            return "admin/admin-category-edit";
        }
    }

    
    
    
}
