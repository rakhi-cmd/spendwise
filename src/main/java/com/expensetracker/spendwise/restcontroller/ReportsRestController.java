package com.expensetracker.spendwise.restcontroller;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.spendwise.dto.ExpenseReportDto;
import com.expensetracker.spendwise.model.Expense;
import com.expensetracker.spendwise.model.User;
import com.expensetracker.spendwise.repository.ExpenseRepository;
import com.expensetracker.spendwise.response.CustomJsonResponse;
import com.expensetracker.spendwise.response.CustomStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/reports")
public class ReportsRestController {
	
	@Autowired
	ExpenseRepository expenseRepository;
	
	private static final Logger logger = LoggerFactory.getLogger(ReportsRestController.class);
	private static final ZoneId ZONE = ZoneId.systemDefault();
	
	@GetMapping("/monthly")
	public ResponseEntity<CustomJsonResponse> monthlyExpenseReport(@RequestParam("month") int month, 
			@RequestParam("year") int year, Authentication auth) {
		logger.info("Monthly report data requested for {}-{}", year, month);
		
		if (month < 1 || month > 12 || year < 2000 || year > Year.now().getValue() + 1) {
			return ResponseEntity.badRequest().body(new CustomJsonResponse(null,
					"Invalid month or year", CustomStatus.ERROR_CODE, CustomStatus.ERROR));
		}
		User loggedInUser = (User) auth.getPrincipal();
		try {
			System.out.println("Logged in user name : "+loggedInUser.getName());
			YearMonth ym = YearMonth.of(year, month);
			Date start = Date.from(ym.atDay(1).atStartOfDay(ZONE).toInstant());
			Date end = Date.from(ym.plusMonths(1).atDay(1).atStartOfDay(ZONE).toInstant());
			System.out.println("Start Date: "+start+"\nEnd Date: "+end);
			
			List<ExpenseReportDto> rows = expenseRepository
					.findMonthlyExpenses(loggedInUser.getName(), start, end)
					.stream()
					.map(this::toDto)
					.collect(Collectors.toList());
			
			return ResponseEntity.ok(new CustomJsonResponse(rows,
					CustomStatus.SUCCESS_MSG, CustomStatus.OK_CODE, CustomStatus.OK));
		} catch (Exception e) {
			logger.error("Failed to load monthly report for {} ({}-{})", loggedInUser.getName(), year, month, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CustomJsonResponse(null,
					CustomStatus.ERROR_MSG, CustomStatus.ERROR_CODE, CustomStatus.ERROR));
		}
	}
	
	private ExpenseReportDto toDto(Expense e) {
		LocalDate date = Instant.ofEpochMilli(e.getExpenseDate().getTime()).atZone(ZONE).toLocalDate();
 
		return new ExpenseReportDto(
				e.getExpenseId(),
				e.getTitle(),
				date.toString(),
				e.getCategory().getName(),                                      
				e.getSubcategory() != null ? e.getSubcategory().getName() : "-",
				e.getPaymentMethod() != null ? e.getPaymentMethod() : "Unknown",
				e.getAmount() != null ? e.getAmount() : BigDecimal.ZERO);
	}
}