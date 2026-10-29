package com.expensetracker.spendwise.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.expensetracker.spendwise.model.Expense;
import com.expensetracker.spendwise.model.User;
import com.expensetracker.spendwise.repository.CategoryRepository;
import com.expensetracker.spendwise.repository.ExpenseRepository;
import com.expensetracker.spendwise.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class DashboardController {

	@Autowired
	CategoryRepository categoryRepo;
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	ExpenseRepository expenseRepo;

	private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

	@GetMapping("/dashboard")
    public ModelAndView dashboard(Authentication authentication) {
		logger.info("Dashboard page called for {} ", (User)authentication.getPrincipal());
        ModelAndView model = new ModelAndView("dashboard");
        boolean admin = hasRole(authentication, "ROLE_ADMIN");
        boolean userRole = hasRole(authentication, "ROLE_USER");
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate nextMonth = monthStart.plusMonths(1);
        LocalDate sixMonthStart = monthStart.minusMonths(5);

        Date todayStart = toDate(today, zone);
        Date tomorrowStart = toDate(today.plusDays(1), zone);
        Date monthStartDate = toDate(monthStart, zone);
        Date nextMonthDate = toDate(nextMonth, zone);
        Date sixMonthDate = toDate(sixMonthStart, zone);

        model.addObject("dashboardPeriod", monthStart.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + monthStart.getYear());
        model.addObject("currentMonthLabel", monthStart.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + monthStart.getYear());

        Long userId = null;
        if (userRole) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                userId = ((User) principal).getId();
            } else {
                User loggedInUser = userRepo.findByUserName(authentication.getName());
                if (loggedInUser != null) userId = loggedInUser.getId();
            }
        }

        if (admin) {
            model.addObject("totalUsers", userRepo.countActiveUsersByRole(1, "USER"));
            model.addObject("totalExpenses", safe(expenseRepo.getActiveExpenseTotal()));
            model.addObject("todayExpenses", safe(expenseRepo.getActiveExpensesBetweenDates(todayStart, tomorrowStart)));
            model.addObject("monthlyExpenses", safe(expenseRepo.getActiveExpensesBetweenDates(monthStartDate, nextMonthDate)));
            model.addObject("totalCategories", categoryRepo.countByStatus(1));
            List<Expense> expenses = expenseRepo.findByStatusAndExpenseDateGreaterThanEqualAndExpenseDateLessThanOrderByExpenseDateAsc(1, sixMonthDate, nextMonthDate);
            addChartData(model, expenses, monthStart, sixMonthStart, zone);
        } else if (userRole && userId != null) {
            model.addObject("myTotalExpenses", safe(expenseRepo.getActiveExpensesByUser(userId)));
            model.addObject("myTodayExpenses", safe(expenseRepo.getActiveExpensesByUserBetweenDates(userId, todayStart, tomorrowStart)));
            model.addObject("myMonthlyExpenses", safe(expenseRepo.getActiveExpensesByUserBetweenDates(userId, monthStartDate, nextMonthDate)));
            model.addObject("myCategories", expenseRepo.countDistinctActiveCategoriesByUserBetweenDates(userId, monthStartDate, nextMonthDate));
            List<Expense> expenses = expenseRepo.findByUser_IdAndStatusAndExpenseDateGreaterThanEqualAndExpenseDateLessThanOrderByExpenseDateAsc(userId, 1, sixMonthDate, nextMonthDate);
            addChartData(model, expenses, monthStart, sixMonthStart, zone);
        } else {
            model.addObject("monthlyLabels", List.of()); model.addObject("monthlyValues", List.of());
            model.addObject("categoryLabels", List.of()); model.addObject("categoryValues", List.of());
            model.addObject("subcategoryLabels", List.of()); model.addObject("subcategoryValues", List.of());
            model.addObject("paymentLabels", List.of()); model.addObject("paymentValues", List.of());
        }
        return model;
    }
	
	private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
    }
	
	private void addChartData(ModelAndView model, List<Expense> expenses, LocalDate currentMonth,
			LocalDate sixMonthStart, ZoneId zone) {
		Map<YearMonth, BigDecimal> monthly = new LinkedHashMap<>();
		for (int i = 0; i < 6; i++) {
			monthly.put(YearMonth.from(sixMonthStart.plusMonths(i)), BigDecimal.ZERO);
		}
		Map<String, BigDecimal> categories = new LinkedHashMap<>();
		Map<String, BigDecimal> subcategories = new LinkedHashMap<>();
		Map<String, BigDecimal> payments = new LinkedHashMap<>();

		for (Expense expense : expenses) {
			if (expense.getExpenseDate() == null || expense.getAmount() == null)
				continue;
			LocalDate date = expense.getExpenseDate().toInstant().atZone(zone).toLocalDate();
			monthly.computeIfPresent(YearMonth.from(date), (key, amount) -> amount.add(expense.getAmount()));
			if (!date.isBefore(currentMonth)) {
				String category = expense.getCategory() == null ? "Uncategorized" : expense.getCategory().getName();
				String subcategory = expense.getSubcategory() == null ? "Other" : expense.getSubcategory().getName();
				String payment = expense.getPaymentMethod() == null || expense.getPaymentMethod().isBlank() ? "Other"
						: expense.getPaymentMethod().replace('_', ' ');
				categories.merge(category, expense.getAmount(), BigDecimal::add);
				subcategories.merge(subcategory, expense.getAmount(), BigDecimal::add);
				payments.merge(payment, expense.getAmount(), BigDecimal::add);
			}
		}
		List<Map.Entry<String, BigDecimal>> cat = top(categories, 6);
		List<Map.Entry<String, BigDecimal>> sub = top(subcategories, 6);
		List<Map.Entry<String, BigDecimal>> pay = top(payments, 6);
		model.addObject("monthlyLabels",
				monthly.keySet().stream()
						.map(ym -> ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH) + " " + ym.getYear())
						.collect(Collectors.toList()));
		model.addObject("monthlyValues", monthly.values().stream().map(BigDecimal::doubleValue).collect(Collectors.toList()));
		model.addObject("categoryLabels", cat.stream().map(Map.Entry::getKey).collect(Collectors.toList()));
		model.addObject("categoryValues", cat.stream().map(e -> e.getValue().doubleValue()).collect(Collectors.toList()));
		model.addObject("subcategoryLabels", sub.stream().map(Map.Entry::getKey).collect(Collectors.toList()));
		model.addObject("subcategoryValues", sub.stream().map(e -> e.getValue().doubleValue()).collect(Collectors.toList()));
		model.addObject("paymentLabels", pay.stream().map(Map.Entry::getKey).collect(Collectors.toList()));
		model.addObject("paymentValues", pay.stream().map(e -> e.getValue().doubleValue()).collect(Collectors.toList()));
	}
	
	private List<Map.Entry<String, BigDecimal>> top(Map<String, BigDecimal> values, int max) {
        return values.entrySet().stream().sorted((a, b) -> b.getValue().compareTo(a.getValue())).limit(max).collect(Collectors.toList());
    }
    private BigDecimal safe(BigDecimal value) { 
    	return value == null ? BigDecimal.ZERO : value; 
	}
    private Date toDate(LocalDate date, ZoneId zone) { 
    	return Date.from(date.atStartOfDay(zone).toInstant()); 
	}
}
