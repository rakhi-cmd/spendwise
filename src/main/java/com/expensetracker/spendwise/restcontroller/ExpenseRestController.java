package com.expensetracker.spendwise.restcontroller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.spendwise.model.Category;
import com.expensetracker.spendwise.model.Expense;
import com.expensetracker.spendwise.model.SubCategory;
import com.expensetracker.spendwise.model.User;
import com.expensetracker.spendwise.repository.CategoryRepository;
import com.expensetracker.spendwise.repository.ExpenseRepository;
import com.expensetracker.spendwise.repository.SubCategoryRepository;
import com.expensetracker.spendwise.response.CustomJsonResponse;
import com.expensetracker.spendwise.response.CustomStatus;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/expense")
public class ExpenseRestController {
	
	private static final Logger logger = LoggerFactory.getLogger(ExpenseRestController.class);
	
	@Autowired
	ExpenseRepository expenseRepo;
	
	@Autowired
	CategoryRepository categoryRepo;
	
	@Autowired
	SubCategoryRepository subCategoryRepo;
	
	@PostMapping("/save")
	public ResponseEntity<CustomJsonResponse> addExpense(@RequestParam("title") String title, @RequestParam("description") String description, 
			@RequestParam("categoryId") long categoryId, @RequestParam("subCategoryId") long subcategoryId, @RequestParam("amount") BigDecimal amount, 
			@RequestParam("expenseDate") String expenseDate,@RequestParam("paymentMethod") String paymentMethod, Authentication authentication) {
		logger.info("Add new expense is called");
		CustomJsonResponse response = new CustomJsonResponse();
		try {
			
			User loggedInUser = (User) authentication.getPrincipal();
			
			// Validate Category
			Optional<Category> categoryData = categoryRepo.findById(categoryId);
			
			// Validate Subcategory
			Optional<SubCategory> subcategoryData = subCategoryRepo.findById(subcategoryId);
			
			if(categoryData.isEmpty() || subcategoryData.isEmpty()) {
				response.setStatus("ERROR");
				response.setStatusCode(0);
				response.setResponse("ERROR");
				response.setResponseMessage("Invalid Category or subcategory.");
				return ResponseEntity.badRequest().body(response);
			}
			Category category = categoryData.get();
			SubCategory subCategory = subcategoryData.get();
			
			// Ensure the subcategory belongs to the selected category
            if (subCategory.getCategory() == null || !subCategory.getCategory().getCategoryId().equals(category.getCategoryId())) {

                response.setResponse("ERROR");
                response.setResponseMessage("Selected subcategory does not belong to this category.");
                response.setStatusCode(0);
                response.setStatus("ERROR");

                return ResponseEntity.badRequest().body(response);
            }
            Expense expense = new Expense();
            expense.setTitle(title);
            expense.setDescription(description);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date expensedate = dateFormat.parse(expenseDate);
            expense.setExpenseDate(expensedate);
            expense.setAmount(amount);
			expense.setCategory(category);
			expense.setSubcategory(subCategory);
			expense.setUser(loggedInUser);
			expense.setPaymentMethod(paymentMethod);
			expense.setStatus(1);
			
			expenseRepo.save(expense);
			
			response.setResponse("SUCCESS");
			response.setResponseMessage("Expense saved successfully.");
			response.setStatus("OK");
			response.setStatusCode(1);
			
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			logger.error("Error saving expense", e);

            response.setResponse("ERROR");
            response.setResponseMessage("Unable to save expense. Please try again.");
            response.setStatusCode(0);
            response.setStatus("ERROR");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
	
	@DeleteMapping("/remove/{expenseId}")
	public ResponseEntity<CustomJsonResponse> removeExpense(@PathVariable long expenseId, Authentication auth){
		logger.info("Remove expense by expenseId: "+expenseId);
		CustomJsonResponse response = new CustomJsonResponse();
		try {
			User loggedInUser = (User) auth.getPrincipal();
			
			Optional<Expense> expenseById = expenseRepo.findById(expenseId);
			if(!expenseById.isEmpty()) {
				Expense e = expenseById.get();
				e.setStatus(0);
				e.setModifiedBy(loggedInUser);
				e.setModifiedDateTime(new Date());
				expenseRepo.save(e);
				
				response.setResponse("OK");
				response.setResponseMessage("Record deleted successfully");
				response.setStatus("SUCCESS");
				response.setStatusCode(1);
				
				return ResponseEntity.ok(response);
			}else {
				response.setResponse("ERROR");
		        response.setResponseMessage("Record not found");
		        response.setStatusCode(0);
		        response.setStatus("FAILED");

		        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
			}
		} catch (Exception e) {
			logger.error("Failed to delete expense: " + expenseId, e);

	        response.setResponse("ERROR");
	        response.setResponseMessage("Failed to delete expense");
	        response.setStatusCode(0);
	        response.setStatus("FAILED");
	        
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
		}
	}
}
