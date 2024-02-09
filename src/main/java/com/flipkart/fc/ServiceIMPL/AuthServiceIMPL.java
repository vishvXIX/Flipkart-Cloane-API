package com.flipkart.fc.ServiceIMPL;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.flipkart.fc.Cache.CacheStore;
import com.flipkart.fc.Entity.AccessToken;
import com.flipkart.fc.Entity.Customer;
import com.flipkart.fc.Entity.RefreshToken;
import com.flipkart.fc.Entity.Seller;
import com.flipkart.fc.Entity.User;
import com.flipkart.fc.Exception.IllagalArgumentException;
import com.flipkart.fc.Exception.UserAlreadyExistException;
import com.flipkart.fc.Exception.UserNotFoundById;
import com.flipkart.fc.Repository.AccessTokenRepository;
import com.flipkart.fc.Repository.RefreshTokenRepository;
import com.flipkart.fc.Repository.UserRepoSitory;
import com.flipkart.fc.RequestDTO.AuthRequest;
import com.flipkart.fc.RequestDTO.OtpModel;
import com.flipkart.fc.RequestDTO.UserRequest;
import com.flipkart.fc.ResponseDTO.AuthResponse;
import com.flipkart.fc.ResponseDTO.UserResponse;
import com.flipkart.fc.Security.JwtService;
import com.flipkart.fc.Service.AuthService;
import com.flipkart.fc.Utility.CookieManager;
import com.flipkart.fc.Utility.MessageStructure;
import com.flipkart.fc.Utility.ResponseStructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthServiceIMPL implements AuthService {

	private PasswordEncoder encoder;
	private UserRepoSitory userRepoSitory;
	private ResponseStructure<UserResponse> structure;
	private ResponseStructure<AuthResponse> authStructure;
	private CacheStore<String> otpCacheStore;
	private CacheStore<User> userCacheStore;
	private JavaMailSender javaMailSender;
	private AuthenticationManager authenticationManager;
	private CookieManager cookieManager;
	private JwtService jwtService;
	private AccessTokenRepository accessTokenRepository;
	private RefreshTokenRepository refreshTokenRepository;
	private AuthResponse authResponse;


	@Value("${myapp.access.expiry}")
	private int accessExpiryInSeconds;
	@Value("${myapp.refresh.expiry}")
	private int refreshExpiryInSeconds;



	public AuthServiceIMPL(PasswordEncoder encoder,
			UserRepoSitory userRepoSitory,
			ResponseStructure<UserResponse> structure,
			ResponseStructure<AuthResponse> authStructure,
			CacheStore<String> otpCacheStore,
			CacheStore<User> userCacheStore, 
			JavaMailSender javaMailSender,
			AuthenticationManager authenticationManager,
			CookieManager cookieManager,
			JwtService jwtService,
			AccessTokenRepository accessTokenRepository,
			RefreshTokenRepository refreshTokenRepository,
			AuthResponse authResponse) {
		super();
		this.encoder = encoder;
		this.userRepoSitory = userRepoSitory;
		this.structure = structure;
		this.authStructure = authStructure;
		this.otpCacheStore = otpCacheStore;
		this.userCacheStore = userCacheStore;
		this.javaMailSender = javaMailSender;
		this.authenticationManager = authenticationManager;
		this.cookieManager = cookieManager;
		this.jwtService = jwtService;
		this.accessTokenRepository = accessTokenRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.authResponse = authResponse;
	}



	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> ragisterUser(UserRequest userrequest) {

		if (userRepoSitory.existsByEmail(userrequest.getEmail())) throw new UserAlreadyExistException("user already exists");

		String otp = generateOtp();
		otpCacheStore.add("key", otp);

		User user = mapToUser(userrequest);
		userCacheStore.add(userrequest.getEmail(),user);
		otpCacheStore.add(userrequest.getEmail(), otp);

		//		try {
		//			sendOtpToMail(user, otp);
		//		} catch (MessagingException e) {
		//			log.error("the email address dose not exists");
		//		}
		//		
		//		try {
		//			conformationMain(user);
		//		} catch (MessagingException e) {
		//			log.error("Ragistration is not Complated");
		//		}

		structure.setStatus(HttpStatus.ACCEPTED.value());
		structure.setMessage("please verify through OTP sent on email id " + otp);
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


	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> findUserById(int userId) throws Exception {

		Optional<User> userOptional = userRepoSitory.findById(userId);
		if (!userOptional.isPresent()) {
			throw new UserNotFoundById("User not found by this Id");
		}

		User user = userOptional.get();
		structure.setStatus(HttpStatus.FOUND.value());
		structure.setMessage("User fatch Sucefully ");
		structure.setData(mapToUserResponce(user));
		return new ResponseEntity<ResponseStructure<UserResponse>>(structure, HttpStatus.FOUND);
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

		if (otp==null) throw new IllagalArgumentException("Otp Expairy");
		if(user==null) throw new IllagalArgumentException("registration Session is expairy");
		if(otp.equals(otpModel.getOtp())) 
		{
			user.setEmailVerified(true);
			userRepoSitory.save(user);

			return new ResponseEntity<ResponseStructure<UserResponse>>(structure,HttpStatus.CREATED);
		}	else
			throw new RuntimeException("INVALID OTP");
	}


	@Override
	public ResponseEntity<ResponseStructure<AuthResponse>> userLogin(AuthRequest authRequest,HttpServletResponse httpServletResponse) {
		String username = authRequest.getEmail().split("@")[0];
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, authRequest.getPassword());
		Authentication authentication = authenticationManager.authenticate(token);

		if(!authentication.isAuthenticated()) {
			throw new UsernameNotFoundException("Failed to authenticate the user");
		}
		else {
			return userRepoSitory.findByUserName(username).map(user -> {
				grantAccess(httpServletResponse, user);
				return ResponseEntity.ok(authStructure.setStatus(HttpStatus.OK.value())
						.setData(authResponse.builder()
								.userId(user.getUserId())
								.username(username)
								.role(user.getUserRole().name())
								.isAuthenticated(true)
								.accessExpiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
								.refreshExpiration(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds))
								.build())
						.setMessage("Login success"));
			}).get();
		}
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
						" Good to see you intrested in flipkart," + 
						" Complate your Registraction using the OTP <br>" +
						"<h1>" + otp + "</h1><br>" + 
						" Note: the otp expire in 1 minute" +
						"<br><br>" + 
						" With best Regards<br>" + 
						" Flipkart"
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


	private void grantAccess(HttpServletResponse response, User user) {

		//		generating access and refresh tokens
		String accessToken = jwtService.generateAccessToken(user.getUserName());
		String refreshToken = jwtService.generateRefreshToken(user.getUserName());

		//		adding access and refresh token in to the response
		response .addCookie(cookieManager.configure(new Cookie("at", accessToken), accessExpiryInSeconds));
		response.addCookie(cookieManager.configure(new Cookie("rt", refreshToken), refreshExpiryInSeconds));

		//		saving the access and refresh cookie in to the database
		accessTokenRepository.save(AccessToken.builder()
				.token(accessToken)
				.isBlocked(false)
				.expiration(LocalDateTime.now().plusSeconds(accessExpiryInSeconds))
				.build());

		refreshTokenRepository.save(RefreshToken.builder()
				.token(refreshToken)
				.isBlocked(false)
				.expiration(LocalDateTime.now().plusSeconds(refreshExpiryInSeconds))
				.build());

	}






}