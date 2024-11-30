package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.nagoyameshi.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User,Integer>{

		    User findByEmail(String email);
	
		 // メールアドレスで部分一致検索
		    List<User> findByEmailContaining(String email);

	
}