package com.example.nagoyameshi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.nagoyameshi.entity.Like;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;

@Repository
public interface LikeRepository extends JpaRepository<Like, Integer> {

    // ユーザーと店舗に基づくLikeを検索
    Optional<Like> findByShopIdAndUserId(Shop shop, User user);

    // ユーザーIDでフィルタリングし、お気に入りの店舗をページネーション付きで取得
    @Query("SELECT s FROM Shop s WHERE s.shopId IN (" +
            "SELECT l.shopId.shopId FROM Like l WHERE l.userId = :user AND l.isLike = 1)")
    List<Shop> findFavoriteShopsByUser(User user, Pageable pageable);

    // ユーザーのお気に入り店舗の総数を取得
    @Query("SELECT COUNT(s) FROM Shop s WHERE s.shopId IN (" +
            "SELECT l.shopId.shopId FROM Like l WHERE l.userId = :user AND l.isLike = 1)")
    int countFavoriteShopsByUser(User user);
    
    
    
    void deleteByShopId(Shop shopId); // shopIdに関連するレコードを削除するメソッド
}
