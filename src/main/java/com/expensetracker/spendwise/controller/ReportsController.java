package com.expensetracker.spendwise.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.expensetracker.spendwise.model.User;
import com.expensetracker.spendwise.repository.ExpenseRepository;

@Controller
@RequestMapping("/reports")
public class ReportsController {

	private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);
	
	@Autowired
	ExpenseRepository expenseRepository;


	@GetMapping("/monthly")
	public ModelAndView monthlyReport(Authentication auth) {

		logger.info("Monthly report requested");
		ModelAndView modelAndView = null;
		try {
			User loggedInUser = (User) auth.getPrincipal();
			System.out.println("Logged in user name: "+loggedInUser.getName()); 
			
			modelAndView = new ModelAndView("reports/monthly");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return modelAndView;
	}
	
	@GetMapping("/yearly")
	public ModelAndView annualReport(Authentication auth) {

		logger.info("Yearly report requested");
		ModelAndView modelAndView = null;
		try {
			User loggedInUser = (User) auth.getPrincipal();
			System.out.println("Logged in user name: "+loggedInUser.getName()); 
			
			modelAndView = new ModelAndView("reports/yearly");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return modelAndView;
	}
}
