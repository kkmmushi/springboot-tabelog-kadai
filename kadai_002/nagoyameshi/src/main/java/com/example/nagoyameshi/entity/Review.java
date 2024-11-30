package com.example.nagoyameshi.entity;

import java.sql.Timestamp;

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
@Table(name="reviews")
@Data

public class Review{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="no")
	private Integer no;
	
	@ManyToOne
	@JoinColumn(name="shop_id")
	private Shop shopId;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User userId;
	
	@Column(name="rate")
	private Integer rate;
	
	@Column(name="review_content")
	private String reviewContent;
	
	@Column(name="created_at", insertable=false, updatable=false)
	private Timestamp createdAt;
	
	@Column(name="updated_at", insertable=false, updatable=false)
	private Timestamp updatedAt;

	public void setUserId(User user) {
		  this.userId = user;
		
	}


	
}