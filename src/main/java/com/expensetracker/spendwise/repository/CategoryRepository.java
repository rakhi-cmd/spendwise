package com.expensetracker.spendwise.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.expensetracker.spendwise.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long>{
	
	//List<Category> findAll();
	@Query(" SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.subcategories ")
	    List<Category> findAllWithSubcategories();
	Category findByCategoryId(long categoryId);
	
	List<Category> findByStatus(int status);
	
	int countByStatus(int status);

}
