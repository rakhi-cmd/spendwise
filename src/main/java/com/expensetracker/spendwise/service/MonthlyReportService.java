package com.expensetracker.spendwise.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expensetracker.spendwise.repository.ExpenseRepository;

@Service
public class MonthlyReportService {
	
	@Autowired
	private ExpenseRepository expenseRepo;

}
