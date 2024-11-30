package com.example.nagoyameshi.entity;

import java.sql.Timestamp;
import java.time.LocalDate;
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
@Table(name="reservations")
@Data

public class Reservation{
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="no")
	private Integer no;
	
	@ManyToOne
	@JoinColumn(name="shop_id")
	private Shop shopId;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private User userId;
	
	@Column(name="reservation_day")
	private LocalDate reservationDay;
	
	@Column(name="reservation_time")
    private LocalTime reservationTime;
	
	  @Column(name="reservation_persons")
	    private Integer reservationPersons; // 追加
	
	@Column(name="created_at", insertable=false, updatable=false)
	private Timestamp createdAt;
	
	@Column(name="updated_at", insertable=false, updatable=false)
	private Timestamp updatedAt;
	
	 @Override
	    public String toString() {
	        return "Reservation{" +
	                "shopId=" + shopId +
	                ", reservationDate='" + reservationDay + '\'' +
	                ", reservationTime='" + reservationTime + '\'' +
	                ", reservationPersons=" + reservationPersons +
	                '}';
	    }
	
}