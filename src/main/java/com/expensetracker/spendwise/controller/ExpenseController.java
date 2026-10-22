package com.expensetracker.spendwise.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.expensetracker.spendwise.model.Category;
import com.expensetracker.spendwise.model.Expense;
import com.expensetracker.spendwise.repository.CategoryRepository;
import com.expensetracker.spendwise.repository.ExpenseRepository;
import com.expensetracker.spendwise.service.ExpenseService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class ExpenseController {
	
	@Autowired
	ExpenseRepository expenseRepo;
	
	@Autowired
	CategoryRepository categoryRepo;
	
	private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);
	
	@GetMapping("/expense")
	public ModelAndView expenses(HttpServletRequest request) {
		logger.info("Expense page called");
		ModelAndView modelAndView = new ModelAndView("expense");
		List<Category> categories = categoryRepo.findByStatus(1);
		modelAndView.addObject("categories", categories);
		
		List<Expense> expenseList = expenseRepo.findAllByOrderByExpenseDateDesc();
		for(Expense expense: expenseList) {
			System.out.println("Category name: "+expense.getCategory().getName());
		}
		modelAndView.addObject("expenseList", expenseList);
		return modelAndView;
	}
	

}
