package com.expensetracker.spendwise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expensetracker.spendwise.model.Category;
import com.expensetracker.spendwise.model.SubCategory;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long>{
	
	boolean existsByNameAndCategory(String name, Category category);

    List<SubCategory> findByCategory(Category category);
    
    int countByStatus(int status);
}
