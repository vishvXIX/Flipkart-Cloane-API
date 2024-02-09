package com.flipkart.fc.Service;

import org.springframework.http.ResponseEntity;

import com.flipkart.fc.RequestDTO.AuthRequest;
import com.flipkart.fc.RequestDTO.OtpModel;
import com.flipkart.fc.RequestDTO.UserRequest;
import com.flipkart.fc.ResponseDTO.AuthResponse;
import com.flipkart.fc.ResponseDTO.UserResponse;
import com.flipkart.fc.Utility.ResponseStructure;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

	ResponseEntity<ResponseStructure<UserResponse>> ragisterUser(UserRequest userRequest);

	ResponseEntity<ResponseStructure<UserResponse>> findUserById(int userId) throws Exception;

	ResponseEntity<ResponseStructure<UserResponse>> deteteUserById(int userId) throws Exception;

	ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OtpModel otpModel);

	public ResponseEntity<ResponseStructure<AuthResponse>> userLogin(AuthRequest authRequest,HttpServletResponse httpServletResponse);
	

}
