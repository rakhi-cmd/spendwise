package com.expensetracker.spendwise.controller;

import java.security.Principal;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.expensetracker.spendwise.model.User;
import com.expensetracker.spendwise.repository.UserRepository;

import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class UserProfileController {
	
	@Autowired
	UserRepository userRepo;
	
	private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);
	
	@GetMapping(value = {"/profile"})
	public ModelAndView profile(Principal principal, Authentication authentication) {
		logger.info("Profile page is called...");
		ModelAndView modelAndView = new ModelAndView("profile");
		try {
			/*
			 * * authentication.getPrincipal() contains the user object * that was loaded
			 * during login. * * We use it ONLY to get the logged-in user's ID.
			 */
			User loggedInUser = (User) authentication.getPrincipal();
			long loggedInUserId = loggedInUser.getId();
			logger.info("Fetching latest profile data from DB for user ID: {}", loggedInUserId);
			/* * Fetch the latest User record from the database. */
			User user = userRepo.findById(loggedInUserId);
			if (user == null) {
				logger.error("User not found in database for ID: {}", loggedInUserId);
				modelAndView.addObject("errorMessage", "Unable to load your profile.");
				return modelAndView;
			}
			logger.info("Latest user data - name: {}, email: {}, phone: {}", user.getName(), user.getEmail(),
					user.getPhone());
			/* * IMPORTANT: * Send the freshly fetched DB user to profile.html. */ modelAndView.addObject("user", user);
		} catch (Exception e) {
			logger.error("Error while loading profile", e);
			modelAndView.addObject("errorMessage", "Unable to load your profile.");
		}
		return modelAndView;
	}
	
	@PostMapping("/profile/update")
	public String updateUserProfile(@ModelAttribute("user") User formUser, 
			Authentication authentication, RedirectAttributes redirectAttributes) {
		logger.info("Profile update request");
		
		try {
			User loggedInUser = (User) authentication.getPrincipal();
			logger.info("logged-in user ID: {}",loggedInUser.getId());
			long loggedInUserId = loggedInUser.getId();
			User existingUser = userRepo.findById(loggedInUserId);
			existingUser.setName(formUser.getName());
			existingUser.setEmail(formUser.getEmail());
			existingUser.setPhone(formUser.getPhone());
			existingUser.setUpdatedBy(loggedInUser);
			existingUser.setUpdatedDate(new Date());
			
			userRepo.save(existingUser);
			// Refresh Spring Security authentication with updated user details
			UsernamePasswordAuthenticationToken newAuthentication =
			        new UsernamePasswordAuthenticationToken(
			                existingUser,
			                existingUser.getPassword(),
			                existingUser.getAuthorities()
			        );

			SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
			securityContext.setAuthentication(newAuthentication);
			SecurityContextHolder.setContext(securityContext);
			logger.info("Profile updated successfully for user ID: {}", existingUser.getId());
			
			// Flash message survives redirect
		    redirectAttributes.addFlashAttribute(
		            "successMessage",
		            "Profile updated successfully!"
		    );
		} catch (Exception e) {
			logger.error("Error while updating profile", e);
			
			redirectAttributes.addFlashAttribute(
		            "errorMessage",
		            "Unable to update your profile. Please try again."
		    );
		}
		
		return "redirect:/profile";
	}
	

}
