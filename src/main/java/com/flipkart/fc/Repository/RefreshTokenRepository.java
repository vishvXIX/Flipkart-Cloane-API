package com.flipkart.fc.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart.fc.Entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

}
