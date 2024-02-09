package com.flipkart.fc.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.flipkart.fc.RequestDTO.AuthRequest;
import com.flipkart.fc.RequestDTO.OtpModel;
import com.flipkart.fc.RequestDTO.UserRequest;
import com.flipkart.fc.ResponseDTO.AuthResponse;
import com.flipkart.fc.ResponseDTO.UserResponse;
import com.flipkart.fc.Service.AuthService;
import com.flipkart.fc.Utility.ResponseStructure;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class AuthController {
	
	private AuthService authService;
	
	@PostMapping("/register")
	public ResponseEntity<ResponseStructure<UserResponse>> ragisterUser(@RequestBody UserRequest userRequest) {
		return authService.ragisterUser(userRequest);
	}
	
	@PostMapping("/verify-otp")
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(@RequestBody OtpModel otpModel) {
		return authService.verifyOTP(otpModel);
	}
	
	@GetMapping("/users/{userId}")
	public ResponseEntity<ResponseStructure<UserResponse>> findUserById (@PathVariable int userId) throws Exception {
		return authService.findUserById(userId);
	}
	
	@DeleteMapping("/users/{userId}")
	public ResponseEntity<ResponseStructure<UserResponse>> deleteUserById (@PathVariable int userId) throws Exception {
		return authService.deteteUserById(userId);
	}
	
	@PostMapping("/login")
	public ResponseEntity<ResponseStructure<AuthResponse>> userLogin(@RequestBody AuthRequest authRequest, HttpServletResponse httpServletResponse){
		return authService.userLogin(authRequest,httpServletResponse);
	}
	
	
}
