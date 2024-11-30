package com.example.nagoyameshi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Shop;

public interface ShopRepository extends JpaRepository<Shop, Integer> {

    // 修正：Category経由でcategoryNoを検索
    Page<Shop> findByCategory_CategoryNo(Integer categoryNo, Pageable pageable);

    Page<Shop> findByCategory_CategoryNoAndShopNameContaining(Integer categoryNo, String search, Pageable pageable);

    Page<Shop> findByShopNameContaining(String search, Pageable pageable);
    

    // 追加：IDによるShopの取得
    Optional<Shop> findById(Integer shopId);  // shopIdがInteger型の場合
    Shop findByShopId(Integer shopId);
    
    
 // カテゴリ番号（categoryNo）で関連する店舗を検索するメソッド
    List<Shop> findByCategoryNo(Integer categoryNo);

}
