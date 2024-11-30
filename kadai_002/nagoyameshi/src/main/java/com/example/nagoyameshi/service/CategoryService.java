package com.example.nagoyameshi.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.repository.CategoryRepository; // CategoryRepositoryのインポート

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category getCategoryById(Integer categoryNo) {
        return categoryRepository.findById(categoryNo).orElse(null); // categoryNoでCategoryを検索
    }
    
    
    // 全カテゴリを取得
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public List<Category> findCategoriesByName(String categoryName) {
        return categoryRepository.findByCategoryNameContaining(categoryName);
    }

    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }
    

    // 仮のメソッド：カテゴリーを保存する
    public void saveCategory(String categoryName) {
        // 実際には、データベースにカテゴリーを保存する処理
        Category category = new Category();
        category.setCategoryName(categoryName);
        categoryRepository.save(category);
    }
    
    // カテゴリ名で検索
    public List<Category> searchCategories(String categoryName) {
        return categoryRepository.findByCategoryNameContaining(categoryName);
    }

    // カテゴリ情報を取得
    public Category getCategoryByNo(Integer categoryNo) {
        return categoryRepository.findById(categoryNo).orElse(null);
    }

    // カテゴリを更新
    public void updateCategory(Category category) {
        categoryRepository.save(category);
    }

    // カテゴリを削除
    public void deleteCategory(Integer categoryNo) {
        categoryRepository.deleteById(categoryNo);
    }
    
    
}
