package com.expensetracker.spendwise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.expensetracker.spendwise.model.Category;
import com.expensetracker.spendwise.model.SubCategory;

public interface SubCategoryRepository extends JpaRepository<SubCategory, Long>{
	
	boolean existsByNameAndCategory(String name, Category category);

    List<SubCategory> findByCategory(Category category);
    
    int countByStatus(int status);
    
    @Query("SELECT COUNT(s) FROM SubCategory s WHERE s.category.categoryId = :categoryId")
    long countSubCategoriesByCategoryId(@Param("categoryId") long categoryId);
}
