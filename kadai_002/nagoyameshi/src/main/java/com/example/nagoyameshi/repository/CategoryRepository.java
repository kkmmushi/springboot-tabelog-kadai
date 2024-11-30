package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.nagoyameshi.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    // カテゴリーIDとカテゴリー名のマップを取得
	@Query("SELECT c.categoryNo, c.categoryName FROM Category c")
	List<Object[]> findCategoryMap();
	
    // カテゴリー名を部分一致で検索
    List<Category> findByCategoryNameContaining(String categoryName);
    
    
}
