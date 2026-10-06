package com.expensetracker.spendwise.restcontroller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.spendwise.model.Category;
import com.expensetracker.spendwise.repository.CategoryRepository;
import com.expensetracker.spendwise.repository.SubCategoryRepository;
import com.expensetracker.spendwise.response.CustomJsonResponse;
import com.expensetracker.spendwise.response.CustomStatus;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/api/categories")
public class CategoryRestController {
	
	@Autowired
	CategoryRepository categoryRepo;
	
	@Autowired
	SubCategoryRepository subCategoryRepo;
	
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
	
	@GetMapping("/status-info/{categoryId}")
	public ResponseEntity<?> getCategoryStatusInfo(@PathVariable long categoryId) {
		logger.info("Get Category Status Info for ID: {}", categoryId);
		try {
			Category category = categoryRepo.findByCategoryId(categoryId);
			if (category == null) {
				return ResponseEntity.badRequest().body(
		                new CustomJsonResponse(null,"Category not found", CustomStatus.ERROR_CODE, CustomStatus.ERROR)
		        );
			}
			long subCategoryCount = subCategoryRepo.countSubCategoriesByCategoryId(categoryId);
			
			Map<String, Object> response = new HashMap<>();

			response.put("categoryId", category.getCategoryId());
			response.put("categoryName", category.getName());
			response.put("status", category.getStatus());
			response.put("subCategoryCount", subCategoryCount);
			return ResponseEntity.ok(
		            new CustomJsonResponse(
		                    response,
		                    "Category status information fetched successfully",
		                    CustomStatus.OK_CODE,
		                    CustomStatus.OK
		            )
		    );
		} catch (Exception e) {
			logger.error("Error while fetching category", e);

	        CustomJsonResponse response = new CustomJsonResponse(null, "Unable to find category",CustomStatus.ERROR_CODE, CustomStatus.ERROR );
	        return ResponseEntity.internalServerError().body(response);
		}
	}
	@PostMapping("/status/{categoryId}")
	public ResponseEntity<?> changeCategoryStatus(@PathVariable long categoryId) {

		logger.info("Change category status API called for ID: {}", categoryId);

		Category category = categoryRepo.findByCategoryId(categoryId);

		if (category == null) {

			return ResponseEntity.badRequest().body(
					new CustomJsonResponse(null, "Category not found", CustomStatus.ERROR_CODE, CustomStatus.ERROR));
		}

		if (category.getStatus() == 1) {
			category.setStatus(0);
		} else {
			category.setStatus(1);
		}

		categoryRepo.save(category);

		return ResponseEntity.ok(new CustomJsonResponse(category, "Category status changed successfully",
				CustomStatus.OK_CODE, CustomStatus.OK));
	}
}
