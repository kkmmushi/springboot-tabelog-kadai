package com.example.nagoyameshi.entity;

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
@Table(name="likes")
@Data

public class Like{
	
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
	
	@Column(name="is_like")
	private Integer isLike;
	
}