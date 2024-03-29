package com.jwt.user.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.jwt.security.config.AppConfig;
import com.jwt.security.config.JwtService;
import com.jwt.user.models.AuthRequest;
import com.jwt.user.models.AuthResponse;
import com.jwt.user.models.RefreshToken;
import com.jwt.user.models.User;
import com.jwt.user.repositories.UserRepo;

@Service
public class AuthService {
	
	@Autowired AppConfig appConfig;
	@Autowired UserRepo userRepo;
	@Autowired JwtService jwtService;
	@Autowired AuthenticationManager authenticationManager;
	@Autowired RefreshTokenService refreshTokenService;
	
	public AuthResponse register(User user) {
		user.setPassword(appConfig.passwordEncoder().encode(user.getPassword()));
//		user.setRole(Role.ADMIN);
		userRepo.save(user);
		return authResponse(user);
	}
	
	public AuthResponse login(AuthRequest authReq) {
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authReq.getEmail(), authReq.getPassword()));
		var user = userRepo.findByEmail(authReq.getEmail()).orElseThrow(()->new UsernameNotFoundException("User not found"));
		return authResponse(user);
	}
	
	public Optional<Object> refreshToken(String refreshToken) {
		 return refreshTokenService.findByToken(refreshToken)
			        .map(refreshTokenService::verifyExpiration)
			        .map(RefreshToken::getUser)
			        .map(user -> {
			        	String token = jwtService.generateToken(user);
			        	return new AuthResponse(token, refreshToken);
			        });
	}
	
	public AuthResponse authResponse(User user) {
		String token = jwtService.generateToken(user);
		String refreshToken = refreshTokenService.createRefreshToken(user.getUserId()).getToken();
		return new AuthResponse(token, refreshToken);
	}
	
}
