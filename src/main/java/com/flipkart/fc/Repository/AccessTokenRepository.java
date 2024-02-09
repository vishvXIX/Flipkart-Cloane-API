package com.flipkart.fc.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart.fc.Entity.AccessToken;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {

}
