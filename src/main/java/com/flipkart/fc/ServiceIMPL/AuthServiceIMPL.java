package com.flipkart.fc.ServiceIMPL;

import java.util.Date;
import java.util.Optional;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.flipkart.fc.Cache.CacheStore;
import com.flipkart.fc.Entity.Customer;
import com.flipkart.fc.Entity.Seller;
import com.flipkart.fc.Entity.User;
import com.flipkart.fc.Exception.IllagalArgumentException;
import com.flipkart.fc.Exception.UserAlreadyExistException;
import com.flipkart.fc.Exception.UserNotFoundById;
import com.flipkart.fc.Repository.UserRepoSitory;
import com.flipkart.fc.RequestDTO.OtpModel;
import com.flipkart.fc.RequestDTO.UserRequest;
import com.flipkart.fc.ResponseDTO.UserResponse;
import com.flipkart.fc.Service.AuthService;
import com.flipkart.fc.Utility.MessageStructure;
import com.flipkart.fc.Utility.ResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class AuthServiceIMPL implements AuthService {

	private PasswordEncoder encoder;
	private UserRepoSitory userRepoSitory;
	private ResponseStructure<UserResponse> structure;
	private CacheStore<String> otpCacheStore;
	private CacheStore<User> userCacheStore;
	private JavaMailSender javaMailSender;


	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> ragisterUser(UserRequest userrequest) {

		if (userRepoSitory.existsByEmail(userrequest.getEmail())) throw new UserAlreadyExistException("user already exists");

		String otp = generateOtp();
		otpCacheStore.add("key", otp);

		User user = mapToUser(userrequest);
		userCacheStore.add(userrequest.getEmail(),user);
		otpCacheStore.add(userrequest.getEmail(), otp);

		try {
			sendOtpToMail(user, otp);
		} catch (MessagingException e) {
			log.error("the email address dose not exists");
		}
		
		try {
			conformationMain(user);
		} catch (MessagingException e) {
			log.error("Ragistration is not Complated");
		}

		structure.setStatus(HttpStatus.ACCEPTED.value());
		structure.setMessage("please verify through OTP sent on email id");
		structure.setData(mapToUserResponce(user));
		return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.ACCEPTED);

	}

	private <T extends User> T mapToUser(UserRequest userRequest) {
		User user = null;
		switch (userRequest.getUserRole()) {
		case CUSTOMER -> {
			user = new Customer();
		}
		case SELLER -> {
			user = new Seller();
		}
		default -> throw new IllagalArgumentException("Unexpected value: " + userRequest.getUserRole());
		}

		user.setEmail(userRequest.getEmail());
		user.setPassword(encoder.encode(userRequest.getPassword()));
		user.setUserRole(userRequest.getUserRole());
		user.setUserName(userRequest.getEmail().split("@")[0]);
		return (T) user;
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> findUserById(int userId) throws Exception {

		Optional<User> userOptional = userRepoSitory.findById(userId);
		if (!userOptional.isPresent()) {
			throw new UserNotFoundById("User not found by this Id");
		}

		User user = userOptional.get();
		structure.setStatus(HttpStatus.CREATED.value());
		structure.setMessage("Sucefully saved User");
		structure.setData(mapToUserResponce(user));
		return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.CREATED);
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> deteteUserById(int userId) throws Exception {

		Optional<User> optionalUser = userRepoSitory.findById(userId);

		if (!optionalUser.isPresent()) {
			throw new UserNotFoundById("User not found by this Id");
		}
		User user = optionalUser.get();

		boolean isEmailVerified = user.isEmailVerified();

		if (isEmailVerified) {

		} else {

		}

		userRepoSitory.delete(user);
		return null;
	}



	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> verifyOTP(OtpModel otpModel) {

		User user = userCacheStore.get(otpModel.getEmail());
		String otp = otpCacheStore.get(otpModel.getEmail());

		if (otp==null) throw new RuntimeException("Otp Expairy");
		if(user==null) throw new RuntimeException("registration Session is expairy");
		if(otp.equals(otpModel.getOtp())) 
		{
			user.setEmailVerified(true);
			userRepoSitory.save(user);

			return new ResponseEntity<ResponseStructure<UserResponse>>(structure,HttpStatus.CREATED);
		}	else
			throw new RuntimeException("INVALID OTP");
	}


	private UserResponse mapToUserResponce(User user) {

		return UserResponse.builder()
				.UserId(user.getUserId())
				.userName(user.getUserName())
				.email(user.getEmail())
				.userRole(user.getUserRole())
				.build();

	}

	private String generateOtp() {
		return String.valueOf(new Random().nextInt(100000,999999));
	}

	@Async
	private void sendMail(MessageStructure message) throws MessagingException {
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
		helper.setTo(message.getTo());
		helper.setSubject(message.getSubject());
		helper.setSentDate(message.getSentDate());
		helper.setText(message.getText(), true);
		javaMailSender.send(mimeMessage);
	}

	private void sendOtpToMail(User user,String otp) throws MessagingException {


		sendMail(MessageStructure.builder()
				.to(user.getEmail())
				.subject("Complate Your Registration to Flipkart")
				.sentDate(new Date())
				.text(
						"Hey," + user.getUserName()+
						"Good to see you intrested in flipkart," + 
						"Complate your Registraction using the OTP <br>" +
						"<h1>" + otp + "</h1><br>" + 
						"Note: the otp expire in 1 minute" +
						"<br><br>" + 
						"With best Regards<br>" + 
						"Flipkart"
						)
				.build());
	} 

	private void conformationMain(User user) throws MessagingException {
		
		sendMail(MessageStructure.builder()
				.to(user.getEmail())
				.subject("Complate Your Registration to Flipkart")
				.sentDate(new Date())
				.text(
						"<h3>Registration Comnfirmation mail</h3>" + "<br>" +
								"Welcome," + user.getUserName()+ "<br>" +
								"Thank you for ragistring in Flipkart" 
						)
				.build());
	}






}