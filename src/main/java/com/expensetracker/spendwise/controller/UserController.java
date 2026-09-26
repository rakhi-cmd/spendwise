package com.expensetracker.spendwise.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.expensetracker.spendwise.model.User;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class UserController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@RequestMapping(value = {"/", "/index"}, method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView home(HttpServletRequest request) {
		logger.info("Home page is called..");
		ModelAndView modelAndView = new ModelAndView("index");
		return modelAndView;
	}
	@RequestMapping(value = {"/login"}, method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView login(HttpServletRequest request) {
		logger.info("Login page is called...");
		ModelAndView modelAndView = new ModelAndView("login");
		return modelAndView;
	}
	
	@GetMapping({"/signup"})
	public ModelAndView signup(HttpServletRequest request) {
		logger.info("Sign up page is called..");
		ModelAndView modelAndView = new ModelAndView("signup");
		modelAndView.addObject("user", new User());
		return modelAndView ;
	}
	
	@RequestMapping(value = {"/403"}, method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView error403(HttpServletRequest request) {
		logger.info("error 403 page is called..");
		ModelAndView modelAndView = new ModelAndView("403");
		return modelAndView ;
	}
	
}
