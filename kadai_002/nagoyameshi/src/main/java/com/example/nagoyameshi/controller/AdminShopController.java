package com.example.nagoyameshi.controller;

import java.time.format.DateTimeFormatter;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.ShopService;

@Controller
@RequestMapping("/admin/shop")
public class AdminShopController {

    @Autowired
    private ShopService shopService;
    
    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String getShopList(
            @RequestParam(value = "shopName", required = false) String shopName,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        // 店舗名で検索（オプション）とページネーション
        Page<Shop> shopsPage = shopService.findShops(shopName, page, 10);

        // モデルにデータを追加
        model.addAttribute("shops", shopsPage.getContent());
        model.addAttribute("shopName", shopName);
        model.addAttribute("totalPages", shopsPage.getTotalPages());
        model.addAttribute("currentPage", page);

        return "admin/admin-shop"; // thymeleafテンプレート名
    }



 // 新規店舗登録画面表示
    @GetMapping("/new")
    public String showCreateShopForm(Model model) {
        // すべてのカテゴリを取得
        List<Category> categories = categoryService.getAllCategories();

        // モデルにカテゴリリストを追加
        model.addAttribute("categories", categories);

        // 新規登録画面のビューを返す
        return "admin/admin-shop-add";
    }


    @PostMapping("/delete/{shopId}")
    public String deleteShop(@PathVariable Integer shopId) {
        shopService.deleteShop(shopId);
        return "redirect:/admin/shop";
    }
    
    
    @GetMapping("/edit/{shopId}")
    public String editShop(@PathVariable Integer shopId, Model model) {
        // 店舗情報をShopServiceを通じて取得
        Shop shop = shopService.getShopById(shopId);
        
        // 店舗情報が見つからなかった場合、エラーページへリダイレクト
        if (shop == null) {
            return "redirect:/admin/shop";  // 店舗一覧ページへリダイレクト
        }

        // カテゴリのリストを取得
        List<Category> categories = categoryService.getAllCategories();
        
        // LocalTime を文字列に変換（nullチェック付き）
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedOpenTime = shop.getOpenTime() != null ? shop.getOpenTime().format(timeFormatter) : "";
        String formattedCloseTime = shop.getCloseTime() != null ? shop.getCloseTime().format(timeFormatter) : "";

        // モデルに必要な属性を追加
        model.addAttribute("shop", shop);
        model.addAttribute("categories", categories);
        model.addAttribute("formattedOpenTime", formattedOpenTime);
        model.addAttribute("formattedCloseTime", formattedCloseTime);

        // 店舗編集ページのビューを返す
        return "admin/admin-shop-edit";
    }
    
    // 店舗編集フォームの送信処理（保存）
    @PostMapping("/edit/{shopId}")
    public String editShop(@PathVariable("shopId") Integer shopId,
                           @ModelAttribute Shop shop,  // フォームの入力値をShopオブジェクトにバインド
                           RedirectAttributes redirectAttributes) {
        try {
            // 店舗情報を更新する
            shopService.updateShop(shopId, shop);
            
            // 更新成功メッセージを追加
            redirectAttributes.addFlashAttribute("message", "店舗情報が更新されました。");
            
            // ダッシュボードまたは店舗リストページにリダイレクト
            return "redirect:/admin/shop";
        } catch (Exception e) {
            // エラーメッセージをリダイレクトして渡す
            redirectAttributes.addFlashAttribute("error", "保存に失敗しました。もう一度お試しください。");
            return "redirect:/admin/shop/edit/" + shopId;
        }
    }
    
    
    @PostMapping("/create")
    public String createShop(@Valid @ModelAttribute("shop") Shop shop, 
                             BindingResult result, 
                             @RequestParam("categoryNo") Integer categoryNo,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        // デバッグ用: フォームから送信されたcategoryNoを表示
        System.out.println("選択されたカテゴリNo: " + shop.getCategoryNo());
        System.out.println("選択されたカテゴリNo: " + categoryNo);
        Category selectedCategory = categoryService.getCategoryById(categoryNo);
        shop.setCategory(selectedCategory);
        
        
        
        // バリデーションエラーがあった場合
        if (result.hasErrors()) {
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            return "admin/admin-shop-add";
        }

        // ここでカテゴリNoを使ってCategoryオブジェクトを取得し、Shopのcategoryに設定
        if (shop.getCategoryNo() != null) {
            // getCategoryByIdメソッドでCategoryを取得
            //Category selectedCategory = categoryService.getCategoryById(shop.getCategoryNo());  //一旦削除
            
            // カテゴリが存在する場合のみ設定
            if (selectedCategory != null) {
                shop.setCategory(selectedCategory);  // Shopのcategoryフィールドにセット
            } else {
                model.addAttribute("errorMessage", "選択したカテゴリが無効です。");
                List<Category> categories = categoryService.getAllCategories();
                model.addAttribute("categories", categories);
                return "admin/admin-shop-add";
            }
        } else {
            model.addAttribute("errorMessage", "カテゴリが選択されていません。");
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            return "admin/admin-shop-add";
        }

        // バリデーションエラーがない場合
        try {
            shopService.saveShop(shop);
            redirectAttributes.addFlashAttribute("successMessage", "店舗登録が完了しました");
            return "redirect:/admin/shop";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "保存に失敗しました。");
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            return "admin/admin-shop-add";
        }
    }

}
