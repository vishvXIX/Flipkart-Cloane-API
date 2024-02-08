package com.flipkart.fc.Exception;

public class IllagalArgumentException extends RuntimeException {

	private String message;

	public IllagalArgumentException(String message) {
		super();
		this.message = message;
	}
	
	public String getMessage() {
		return message;
	}
	
}
