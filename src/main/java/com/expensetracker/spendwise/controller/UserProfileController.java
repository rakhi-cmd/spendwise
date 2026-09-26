package com.expensetracker.spendwise.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.expensetracker.spendwise.model.User;
import com.expensetracker.spendwise.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UserProfileController {
	
	@Autowired
	UserRepository userRepo;
	
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@GetMapping(value = {"/profile"})
	public ModelAndView profile(Principal principal, Authentication authentication) {
		logger.info("Profile page is called...");
		ModelAndView modelAndView = new ModelAndView("profile"); 
		String username = authentication.getName(); 
		System.out.println("Logged in user name: "+username);
		User user = (User) authentication.getPrincipal();
		System.out.println("user name: "+user.getName()+" user email: "+user.getEmail());
		
		modelAndView.addObject("user", user); 
		return modelAndView;
	}

}
