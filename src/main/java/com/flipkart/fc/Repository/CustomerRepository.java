package com.flipkart.fc.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.flipkart.fc.Entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer>{

}
