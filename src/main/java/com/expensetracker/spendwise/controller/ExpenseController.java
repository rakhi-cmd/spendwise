package com.expensetracker.spendwise.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
		
		//Load categories
		List<Category> categories = categoryRepo.findByStatus(1);
		modelAndView.addObject("categories", categories);
		
		//Load expenses
		List<Expense> expenseList = expenseRepo.findAllByOrderByExpenseDateDesc();
		
		// Initialize summary values
		/*
		 * BigDecimal totalExpenses = BigDecimal.ZERO; BigDecimal monthlyExpenses =
		 * BigDecimal.ZERO; BigDecimal todayExpenses = BigDecimal.ZERO;
		 * 
		 * Map<String, BigDecimal> categoryTotals = new HashMap<>();
		 * 
		 * // Get today's date Date today = new Date();
		 * 
		 * // Get start of today Calendar calendar = Calendar.getInstance();
		 * calendar.setTime(today); calendar.set(Calendar.HOUR_OF_DAY, 0);
		 * calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0);
		 * calendar.set(Calendar.MILLISECOND, 0);
		 * 
		 * Date todayStart = calendar.getTime();
		 * 
		 * // Get start of tomorrow calendar.add(Calendar.DAY_OF_MONTH, 1); Date
		 * tomorrowStart = calendar.getTime();
		 * 
		 * // Get start of current month calendar.setTime(todayStart);
		 * calendar.set(Calendar.DAY_OF_MONTH, 1); Date monthStart = calendar.getTime();
		 * 
		 * // Get start of next month calendar.add(Calendar.MONTH, 1); Date
		 * nextMonthStart = calendar.getTime();
		 * 
		 * for(Expense expense : expenseList) { BigDecimal amount = expense.getAmount()
		 * != null ? expense.getAmount() : BigDecimal.ZERO; Date expenseDate =
		 * expense.getExpenseDate();
		 * 
		 * //Total Expenses totalExpenses = totalExpenses.add(amount);
		 * 
		 * if(expenseDate != null) { //2. This month expense
		 * if(!expenseDate.before(monthStart) && expenseDate.before(nextMonthStart)) {
		 * monthlyExpenses = monthlyExpenses.add(amount);
		 * 
		 * // Calculate category totals for this month if (expense.getCategory() !=
		 * null) {
		 * 
		 * String categoryName = expense.getCategory().getName();
		 * categoryTotals.merge(categoryName, amount, BigDecimal::add); } } // 3.
		 * Today's expenses if (!expenseDate.before(todayStart) &&
		 * expenseDate.before(tomorrowStart)) {
		 * 
		 * todayExpenses = todayExpenses.add(amount); } } } // 4. Find top category
		 * String topCategory = "-"; BigDecimal topCategoryAmount = BigDecimal.ZERO;
		 * 
		 * for (Map.Entry<String, BigDecimal> entry : categoryTotals.entrySet()) {
		 * 
		 * if (entry.getValue().compareTo(topCategoryAmount) > 0) { topCategory =
		 * entry.getKey(); topCategoryAmount = entry.getValue(); } }
		 */
		// 3. Calculate date boundaries using Date and Calendar
		Date today = new Date();

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(today);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		Date todayStart = calendar.getTime();

		calendar.add(Calendar.DAY_OF_MONTH, 1);
		Date tomorrowStart = calendar.getTime();

		calendar.setTime(todayStart);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		Date monthStart = calendar.getTime();

		calendar.add(Calendar.MONTH, 1);
		Date nextMonthStart = calendar.getTime();

		// 4. Calculate summary values using JPA
		BigDecimal totalExpenses = expenseRepo.getTotalExpenses();

		BigDecimal monthlyExpenses = expenseRepo.getExpensesBetweenDates(monthStart, nextMonthStart);

		BigDecimal todayExpenses = expenseRepo.getExpensesBetweenDates(todayStart, tomorrowStart);

		// 5. Find the top category for the current month
		List<Object[]> topCategories = expenseRepo.findCategoryTotalsBetweenDates(monthStart, nextMonthStart, PageRequest.of(0, 1));

		String topCategory = "-";
		BigDecimal topCategoryAmount = BigDecimal.ZERO;

		if (!topCategories.isEmpty()) {
			Object[] result = topCategories.get(0);

			topCategory = (String) result[0];

			topCategoryAmount = result[1] != null ? (BigDecimal) result[1] : BigDecimal.ZERO;
		}
	    // Pass summary data to the page
	    modelAndView.addObject("totalExpenses", totalExpenses);
	    modelAndView.addObject("monthlyExpenses", monthlyExpenses);
	    modelAndView.addObject("todayExpenses", todayExpenses);
	    modelAndView.addObject("topCategory", topCategory);
	    modelAndView.addObject("topCategoryAmount", topCategoryAmount);

	    // Current month label
	    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy");
	    modelAndView.addObject("currentMonth", monthFormat.format(today));
	    
	    // Pass expense list to the page
	 	modelAndView.addObject("expenseList", expenseList);
	 	
		return modelAndView;
	}
}
