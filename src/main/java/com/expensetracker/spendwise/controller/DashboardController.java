package com.expensetracker.spendwise.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class DashboardController {
	
	private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
	
	@RequestMapping(value = {"/dashboard"}, method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView dashboard(Authentication authentication, Principal principle) {
		
		logger.info("Common dashboard page is called...");
		ModelAndView modelAndView = new ModelAndView("dashboard");
		
		String userName = principle.getName();
		logger.info("Logged in user {}", userName);
		
		boolean isAdmin =
                authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(
                                role -> role.equals("ROLE_ADMIN")
                        );


        boolean isUser =
                authentication.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(
                                role -> role.equals("ROLE_USER")
                        );
        // Admin //
        if(isAdmin) {
        	logger.info("Admin dashboard loaded for {}",userName);
        	
        	modelAndView.addObject("totalUsers", 0);

            modelAndView.addObject("totalExpenses", 0);

            modelAndView.addObject("totalCategories", 0);

            modelAndView.addObject("monthlyExpenses", 0);
        // User //
        }else if (isUser) {
        	logger.info("User dashboard loaded for {}",userName);

            // Temporary values
            // Replace these with service calls

            modelAndView.addObject("myTotalExpenses", 0);

            modelAndView.addObject("myMonthlyExpenses", 0);

            modelAndView.addObject("myCategories", 0);

            modelAndView.addObject("remainingBudget", 0);
			
		}
        // ==========================================
        // UNKNOWN ROLE
        // ==========================================

        else {

            logger.warn("User has no valid role: {}",
                    userName
            );

        }
        
		return modelAndView ;
	}

}
