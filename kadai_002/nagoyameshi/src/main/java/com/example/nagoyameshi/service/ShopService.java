package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.CategoryRepository;
import com.example.nagoyameshi.repository.LikeRepository;
import com.example.nagoyameshi.repository.ReservationRepository;
import com.example.nagoyameshi.repository.ReviewRepository;
import com.example.nagoyameshi.repository.ShopRepository;

@Service
public class ShopService {

    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final LikeRepository likeRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;  // ReservationRepositoryを追加

    @Autowired
    private ReviewRepository reviewRepository;  // ReviewRepositoryを追加

    @Autowired
    public ShopService(ShopRepository shopRepository, CategoryRepository categoryRepository, LikeRepository likeRepository) {
        this.shopRepository = shopRepository;
        this.categoryRepository = categoryRepository;
        this.likeRepository = likeRepository;
    }

    // CategoryのcategoryNoを使ってShopを取得
    public Page<Shop> getShopsByCategory(Integer categoryNo, Pageable pageable) {
        return shopRepository.findByCategory_CategoryNo(categoryNo, pageable);
    }

    // CategoryのcategoryNoとShop名で部分一致検索
    public Page<Shop> getShopsByCategoryAndSearch(Integer categoryNo, String search, Pageable pageable) {
        return shopRepository.findByCategory_CategoryNoAndShopNameContaining(categoryNo, search, pageable);
    }

    // 店舗名で部分一致検索
    public Page<Shop> searchShops(String search, Pageable pageable) {
        return shopRepository.findByShopNameContaining(search, pageable);
    }

    // すべてのShopを取得
    public Page<Shop> getAllShops(Pageable pageable) {
        return shopRepository.findAll(pageable);
    }

    // カテゴリを取得
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    // 店舗IDで検索
    public Shop getShopById(Integer shopId) {
        return shopRepository.findById(shopId).orElse(null);
    }

    // CategoryのMapを取得
    public Map<Integer, String> getCategoryMap1() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                         .collect(Collectors.toMap(Category::getCategoryNo, Category::getCategoryName));
    }
    
    // ユーザーのお気に入り店舗をページネーションに基づいて取得
    public List<Shop> getFavoriteShopsForUser(User user, Pageable pageable) {
        return likeRepository.findFavoriteShopsByUser(user, pageable);
    }

    // ユーザーのお気に入り店舗の総数を取得
    public int getTotalFavoriteShopsForUser(User user) {
        return likeRepository.countFavoriteShopsByUser(user);
    }

    // カテゴリーマップを取得（任意）
    public Map<Integer, String> getCategoryMap() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                         .collect(Collectors.toMap(Category::getCategoryNo, Category::getCategoryName));
    }
    
    
    
 // 店舗情報を取得
    public Shop findShopById(Integer shopId) {
        return shopRepository.findByShopId(shopId);
    }
    
 // 店舗名で検索し、ページネーション対応
    public Page<Shop> findShops(String shopName, int page, int size) {
        if (shopName == null || shopName.isEmpty()) {
            return shopRepository.findAll(PageRequest.of(page, size, Sort.by("shopId").ascending()));
        } else {
            return shopRepository.findByShopNameContaining(shopName, PageRequest.of(page, size, Sort.by("shopId").ascending()));
        }
    }

    public Shop saveShop(Shop shop) {
        return shopRepository.save(shop);
    }

    public Shop findById(Integer shopId) {
        return shopRepository.findById(shopId).orElseThrow(() -> new RuntimeException("Shop not found"));
    }

    @Transactional
    public void deleteShop(Integer shopId) {
 
    	
        Shop shop = shopRepository.findByShopId(shopId);
        
        System.out.println("消そうとしているのは"+shop);
    	
    	// 1. まず、likesテーブルから関連レコードを削除
        likeRepository.deleteByShopId(shop);
        

        
     // 1. 予約データを削除
        List<Reservation> reservations = reservationRepository.findByShopId(shop);
        reservationRepository.deleteAll(reservations);

        // 2. レビューデータを削除
        List<Review> reviews = reviewRepository.findByShopId(shop);
        reviewRepository.deleteAll(reviews);

        
        System.out.println("消そうとしているのは"+shopId);

        // shopテーブルからレコードを削除
        shopRepository.deleteById(shopId);
    }
    
    // 店舗情報を更新
    public void updateShop(Integer shopId, Shop shop) {
        Shop existingShop = getShopById(shopId);
        
        // 必要なフィールドを更新
        existingShop.setShopName(shop.getShopName());
        existingShop.setCategoryNo(shop.getCategoryNo());
        existingShop.setOpenTime(shop.getOpenTime());
        existingShop.setCloseTime(shop.getCloseTime());
        existingShop.setAddress(shop.getAddress());
        existingShop.setPhoneNumber(shop.getPhoneNumber());
        existingShop.setHoliday(shop.getHoliday());
        existingShop.setMinPrice(shop.getMinPrice());
        existingShop.setMaxPrice(shop.getMaxPrice());
        existingShop.setDescription(shop.getDescription());
        
        shopRepository.save(existingShop);  // 更新された店舗情報を保存
    }
    
}
