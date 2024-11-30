package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.repository.ReviewRepository;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;  // レビューを保存するリポジトリ


    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }
    
    
    // レビュー保存メソッド
    public void saveReview(Review review) {
        reviewRepository.save(review);
    }

	public List<Review> findByUser(User user) {
	
		  return reviewRepository.findByUserId(user);
	}
	
	
	// レビューを削除するメソッド
    public void deleteReview(Integer reviewNo) {
        // レビューが存在するか確認して削除
        Optional<Review> review = reviewRepository.findByNo(reviewNo);
        if (review.isPresent()) {
            reviewRepository.delete(review.get());
        } else {
            throw new RuntimeException("レビューが見つかりません");
        }
    }
    
    
    // レビューをIDで検索
    public Review findByNo(Integer reviewNo) {
        return reviewRepository.findByNo(reviewNo).orElse(null);
    }


	
}
