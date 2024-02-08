package com.flipkart.fc.Exception;

public class UserNotFoundById extends Exception {

	private String message ;

	public UserNotFoundById(String message) {
		super();
		this.message=message;
	}

	public String getMessage() {
		return message;
	}
	
}
