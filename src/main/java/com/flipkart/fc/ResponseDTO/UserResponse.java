package com.flipkart.fc.ResponseDTO;

import com.flipkart.fc.Enum.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

	private int UserId;
	private String userName;	
	private String email;
	private UserRole userRole;
	private boolean isEmailVerified;
	private boolean isDeleted;
	
}
