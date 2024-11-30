package com.example.nagoyameshi.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Data;

@Entity
@Table(name="shops")
@Data
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="shop_id")
    private Integer shopId;

    @ManyToOne
    @JoinColumn(name = "category_no", referencedColumnName = "no")
    private Category category;  // Category型のフィールドとして保持


    @Column(name="category_no", insertable = false, updatable = false)
    private Integer categoryNo;

    @Column(name="shop_name")
    private String shopName;

    @Column(name="image_name")
    private String imageName;

    @Column(name="description")
    private String description;

    @Column(name="open_time")
    private LocalTime openTime;

    @Column(name="close_time")
    private LocalTime closeTime;

    @Column(name="min_price")
    private Integer minPrice;

    @Column(name="max_price")
    private Integer maxPrice;

    @Column(name="postal_code")
    private String postalCode;

    @Column(name="address")
    private String address;

    @Column(name="phone_number")
    private String phoneNumber;

    @Column(name="holiday")
    private String holiday;

    @Column(name="created_at", insertable = false, updatable = false)
    private String createdAt;

    @Column(name="updated_at", insertable = false, updatable = false)
    private String updatedAt;

    // CategoryからcategoryNoを取得するメソッド
    public Integer getCategoryNo() {
        return category != null ? category.getCategoryNo() : null;
    }

    // CategoryからcategoryNameを取得するメソッド
    public String getCategoryName() {
        return category != null ? category.getCategoryName() : null;
    }
}
