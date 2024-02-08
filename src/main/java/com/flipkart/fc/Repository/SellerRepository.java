package com.flipkart.fc.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart.fc.Entity.Seller;

public interface SellerRepository extends JpaRepository<Seller, Integer> {
	
	
}
