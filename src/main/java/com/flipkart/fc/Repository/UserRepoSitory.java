package com.flipkart.fc.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart.fc.Entity.User;

public interface UserRepoSitory extends JpaRepository<User, Integer> {

	boolean existsByEmail(String email);

	boolean existsByUserName(String username);

	Optional<User> findByUserName(String string);

	Optional<User> findByEmail(String email);

	

}
