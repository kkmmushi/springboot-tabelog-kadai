package com.example.nagoyameshi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 店舗IDでレビューを取得
    List<Review> findByShopId(Shop shopId);

    // ユーザーIDでレビューを取得
    List<Review> findByUserId(User user);

    // レビュー番号でレビューを取得
    Optional<Review> findByNo(Integer reviewNo);

    // 店舗IDでレビューを取得し、作成日時で降順に並べる（すべてのレビューを取得）
    List<Review> findByShopIdOrderByCreatedAtDesc(Shop shopId);
    
    // 店舗IDでレビューを取得し、作成日時で降順に並べる（最新3件を取得）
    List<Review> findTop3ByShopIdOrderByCreatedAtDesc(Shop shopId);
}
