package com.expensetracker.spendwise.restcontroller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expensetracker.spendwise.model.Category;
import com.expensetracker.spendwise.model.SubCategory;
import com.expensetracker.spendwise.model.User;
import com.expensetracker.spendwise.repository.CategoryRepository;
import com.expensetracker.spendwise.repository.SubCategoryRepository;
import com.expensetracker.spendwise.response.CustomJsonResponse;
import com.expensetracker.spendwise.response.CustomStatus;

@RestController
@RequestMapping("/api/subcategory")
public class SubCategoryRestController {
	@Autowired
	SubCategoryRepository subCategoryRepo;
	
	@Autowired
	CategoryRepository categoryRepo;
	
	private static final Logger logger = LoggerFactory.getLogger(SubCategoryRestController.class);
	
	@PostMapping("/save")
	public ResponseEntity<?> addSubcategory(@RequestParam("categoryId") long categoryId, @RequestParam("name") String name,
			@RequestParam(value = "description", required = false) String description,
            @RequestParam("status") int status, Authentication authentication){
		logger.info("add subcategory api is called..");
		try {
			User loggedInUser = (User) authentication.getPrincipal();
			Optional<Category> categoryById = categoryRepo.findById(categoryId);
			if(categoryById.isEmpty()) {
				CustomJsonResponse response = new CustomJsonResponse(null, "Category not found", 
						CustomStatus.ERROR_CODE, CustomStatus.ERROR);
				return ResponseEntity.badRequest().body(response);
			}
			Category category = categoryById.get();
			boolean exists = subCategoryRepo.existsByNameAndCategory(name.trim(), category);
			if(exists) {
				CustomJsonResponse response = new CustomJsonResponse(null, "Subcategory already exists under this category", 
						CustomStatus.ERROR_CODE, CustomStatus.ERROR);
				return ResponseEntity.ok(response);
			}
			SubCategory subCategory = new SubCategory();
			subCategory.setCategory(category);
			subCategory.setName(name.trim());
			subCategory.setStatus(status);
			subCategory.setDescription(description);
			subCategory.setCreatedBy(loggedInUser);
			SubCategory savedSubCategory = subCategoryRepo.save(subCategory);
			
			Map<String, Object> data = new HashMap<>();

			data.put("subCategoryId", savedSubCategory.getSubCategoryId());
			data.put("name", savedSubCategory.getName());
			data.put("description", savedSubCategory.getDescription());
			data.put("status", savedSubCategory.getStatus());

			CustomJsonResponse response = new CustomJsonResponse(
			    data,
			    "Subcategory saved successfully",
			    CustomStatus.OK_CODE,
			    CustomStatus.OK
			);
			/*CustomJsonResponse customResponse = new CustomJsonResponse(savedSubCategory, "Subcategory saved successfully", 
					CustomStatus.ERROR_CODE, CustomStatus.ERROR);*/
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			logger.error("Error while saving subcategory", e);

	        CustomJsonResponse response = new CustomJsonResponse(
	                null,
	                "Unable to save subcategory",
	                CustomStatus.ERROR_CODE,
	                CustomStatus.ERROR
	        );
	        return ResponseEntity.internalServerError().body(response);
		}
	}
}
