package com.expensetracker.spendwise.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expensetracker.spendwise.model.Expense;
import com.expensetracker.spendwise.repository.ExpenseRepository;

@Service
public class ExpenseService {
	
	@Autowired
	ExpenseRepository expenseRepo;
	
	public Expense saveExpense(Expense expense) {
		return expenseRepo.save(expense);
	}
	public List<Expense> getAllExpenses(){
		return expenseRepo.findAll();
	}
	
	public void deleteExpense(Long id) {
		expenseRepo.deleteById(id);
	}
	
	Optional<Expense> getExpenseById(Long id){
		return expenseRepo.findById(id);
	}

}
