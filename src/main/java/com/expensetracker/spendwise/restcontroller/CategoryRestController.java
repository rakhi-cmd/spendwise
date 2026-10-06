package com.expensetracker.spendwise.restcontroller;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.spendwise.model.Category;
import com.expensetracker.spendwise.repository.CategoryRepository;
import com.expensetracker.spendwise.response.CustomJsonResponse;
import com.expensetracker.spendwise.response.CustomStatus;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/categories")
public class CategoryRestController {
	
	@Autowired
	CategoryRepository categoryRepo;
	
	private static final Logger logger = LoggerFactory.getLogger(CategoryRestController.class);
	
	@GetMapping("/get/{categoryId}")
	public ResponseEntity<?> getCategories(@PathVariable Long categoryId) {
		logger.info("Get Category by category ID");
		try {
			Optional<Category> getCategoryByCategoryId = categoryRepo.findById(categoryId);
			if(getCategoryByCategoryId.isEmpty()) {
				CustomJsonResponse response = new CustomJsonResponse(null, "Category not found", 
						CustomStatus.ERROR_CODE, CustomStatus.ERROR);
				return ResponseEntity.badRequest().body(response);
			}else {
				CustomJsonResponse response = new CustomJsonResponse(getCategoryByCategoryId.get(),"Category found successfully.",
						CustomStatus.OK_CODE, CustomStatus.OK);
				return ResponseEntity.ok(response);
			}
		} catch (Exception e) {
			logger.error("Error while fetching category", e);

	        CustomJsonResponse response = new CustomJsonResponse(null, "Unable to find category",CustomStatus.ERROR_CODE, CustomStatus.ERROR );
	        return ResponseEntity.internalServerError().body(response);
		}
	}
}
