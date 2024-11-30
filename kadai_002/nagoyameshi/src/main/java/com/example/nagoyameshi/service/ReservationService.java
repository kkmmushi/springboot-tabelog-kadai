package com.example.nagoyameshi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.ReservationRepository;

@Service
public class ReservationService {
	
	@Autowired
    private ReservationRepository reservationRepository;

	
	   // 予約を保存
    public Reservation saveReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }
    
    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> findByUser(User user) {
        return reservationRepository.findByUserId(user);
    }
    
    // 予約削除処理
    public boolean cancelReservation(Long reservationNo) {
        try {
            reservationRepository.deleteById(reservationNo);  // 予約を削除
            return true;
        } catch (Exception e) {
            return false;  // 削除失敗の場合
        }
    }
    
}
