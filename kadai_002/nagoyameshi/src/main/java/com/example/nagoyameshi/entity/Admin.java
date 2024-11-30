package com.example.nagoyameshi.entity;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;


@Entity
@Table(name="admin")
@Data
public class Admin{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="no")
	private Integer no;
	
	@Column(name="email")
	private String email;
	
	@Column(name="password")
	private String password;
	
	@Column(name="created_at", insertable=false, updatable=false)
	private Timestamp createdAt;
	
}