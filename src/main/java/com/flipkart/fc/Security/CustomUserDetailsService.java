package com.flipkart.fc.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.flipkart.fc.Repository.UserRepoSitory;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepoSitory userrepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userrepository.findByUserName(username).map(user->new CustomUserDetails(user)).orElseThrow(
				()-> new UsernameNotFoundException("Failed to authenticate the user"));
	}
	

}
