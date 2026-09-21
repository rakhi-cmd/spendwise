package com.expensetracker.spendwise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;


@Controller
public class UserController {
	
	@RequestMapping(value = {"/", "/index"}, method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView home(HttpServletRequest request) {
		System.out.println("Inside home page");
		ModelAndView modelAndView = new ModelAndView("index");
		return modelAndView;
	}
	@RequestMapping(value = {"/login"}, method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView login(HttpServletRequest request) {
		System.out.println("Inside login page");
		ModelAndView modelAndView = new ModelAndView("login");
		return modelAndView;
	}
	
	@RequestMapping(value = {"/signup"}, method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView signup(HttpServletRequest request) {
		System.out.println("Sign up page");
		ModelAndView modelAndView = new ModelAndView("signup");
		return modelAndView ;
	}
	
	@RequestMapping(value = {"/dashboard"}, method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView dashboard(HttpServletRequest request) {
		System.out.println("dashboard page");
		ModelAndView modelAndView = new ModelAndView("dashboard");
		return modelAndView ;
	}
	
	@RequestMapping(value = {"/403"}, method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView error403(HttpServletRequest request) {
		System.out.println("error403 page");
		ModelAndView modelAndView = new ModelAndView("403");
		return modelAndView ;
	}
	

}
