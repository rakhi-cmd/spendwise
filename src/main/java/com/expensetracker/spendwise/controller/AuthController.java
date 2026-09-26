package com.expensetracker.spendwise.controller;

import java.util.Arrays;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;

import com.expensetracker.spendwise.model.User;
import com.expensetracker.spendwise.repository.UserRepository;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class AuthController {
	
	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@PostMapping("/register")
	public ModelAndView registerUser(@ModelAttribute("user") User user, 
			@RequestParam("confirmPassword") String confirmPassword) {
		System.out.println("Registering a new user : "+user.getName());
		// check for password
		User newUser = new User();
		if(user.getPassword() == null || !user.getPassword().equals(confirmPassword)) {
			return signupWithError(user, "Passwords do not match.");
		}else {
			newUser.setPassword(passwordEncoder.encode(user.getPassword()));
		}
		
		// username check
		
		if(userRepo.existsByUserName(user.getName())) {
			return signupWithError(user, "Name is already registered.");
		}else {
			newUser.setName(user.getName());
		}
		
		// email check
		
		if(userRepo.existsByEmail(user.getEmail())) {
			return signupWithError(user, "Email is already registered.");
		}else {
			newUser.setEmail(user.getEmail());
		}
		newUser.setRoles(Arrays.asList("ROLE_USER"));
		newUser.setStatus(1);
		userRepo.save(newUser);
		
		ModelAndView modelAndView = new ModelAndView("login");
		modelAndView.addObject("success", "Registration successfull! Please login.");
		
		return modelAndView;
	}
	
	private ModelAndView signupWithError(User user, String errorMessage) {
		ModelAndView modelAndView = new ModelAndView("signup");
		modelAndView.addObject("user", user);
		modelAndView.addObject("error", errorMessage);
		return modelAndView;
	}
	

}
