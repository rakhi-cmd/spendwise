package com.expensetracker.spendwise.controller;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.expensetracker.spendwise.model.Category;
import com.expensetracker.spendwise.model.User;
import com.expensetracker.spendwise.repository.CategoryRepository;
import com.expensetracker.spendwise.repository.SubCategoryRepository;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class CategoryController {
	
	@Autowired
	CategoryRepository categoryRepo;
	
	@Autowired
	SubCategoryRepository subCategoryRepo;
	
	private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
	
	@GetMapping("/categories")
	public ModelAndView categories(HttpServletRequest request) {
		logger.info("Manage category page is called..");
		ModelAndView modelAndView = new ModelAndView("categories");
		List<Category>  categories = categoryRepo.findAllWithSubcategories();
		for (Category category : categories) {
			System.out.println("Category ID :"+category.getCategoryId());
			System.out.println("Category name :"+category.getName());
			System.out.println("Category status :"+category.getStatus());
			
			if (category.getSubcategories() != null) {
	            System.out.println(
	                "Subcategory count :" + category.getSubcategories().size()
	            );
	        }
		}
		long totalCategory = categoryRepo.count();
		int totalActiveCategory = categoryRepo.countByStatus(1);
		int totalActiveSubcategory = subCategoryRepo.countByStatus(1);
		int totalInactiveCategory = categoryRepo.countByStatus(0);
		modelAndView.addObject("totalCategory", totalCategory);
		modelAndView.addObject("totalActiveCategory", totalActiveCategory);
		modelAndView.addObject("totalActiveSubcategory", totalActiveSubcategory);
		modelAndView.addObject("totalInactiveCategory", totalInactiveCategory);
		modelAndView.addObject("categories", categories);
		return modelAndView ;
	}
	
	// Add new category using simple form submit
    @PostMapping("/categories/add")
    public String addCategory(@ModelAttribute("category") Category category, Authentication authentication) {
    	logger.info("Add new category");
    	User loggedInUser = (User) authentication.getPrincipal();
    	
		logger.info("logged-in user ID: {}",loggedInUser.getId());
		
    	Category addNewCategory = new Category();
    	addNewCategory.setName(category.getName());
    	addNewCategory.setDescription(category.getDescription());
    	addNewCategory.setStatus(category.getStatus());
    	addNewCategory.setCreatedBy(loggedInUser);
    	
    	categoryRepo.save(addNewCategory);

        return "redirect:/categories";
    }
    
    @GetMapping("/categories/get")
    public String getMethodName(@RequestParam String param) {
        return new String();
    }
    
	

}
