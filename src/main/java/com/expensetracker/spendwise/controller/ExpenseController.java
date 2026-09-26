package com.expensetracker.spendwise.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.expensetracker.spendwise.service.ExpenseService;

@Controller
public class ExpenseController {
	
	@Autowired
	ExpenseService expenseService;
	
	private static final Logger logger = LoggerFactory.getLogger(ExpenseController.class);

}
