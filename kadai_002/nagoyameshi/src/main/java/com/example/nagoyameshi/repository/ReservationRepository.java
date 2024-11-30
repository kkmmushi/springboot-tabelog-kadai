
package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Shop;
import com.example.nagoyameshi.entity.User;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 店舗IDによる予約取得メソッド等があれば追加
	
	 List<Reservation> findByUserId(User user);
	 
	// 店舗IDで予約を取得
	    List<Reservation> findByShopId(Shop shopId);

}
