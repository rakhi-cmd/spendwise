package com.expensetracker.spendwise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AdminController {
	
	@RequestMapping(value = {"/admin/dashboard"}, method = {RequestMethod.GET, RequestMethod.POST})
	public ModelAndView error403(HttpServletRequest request) {
		System.out.println("error403 page");
		ModelAndView modelAndView = new ModelAndView("admin/dashboard");
		return modelAndView ;
	}
	

}
