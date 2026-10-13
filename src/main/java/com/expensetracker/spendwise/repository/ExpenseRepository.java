package com.expensetracker.spendwise.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expensetracker.spendwise.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long>{
	
	List<Expense> findByUser_IdOrderByExpenseDateDesc(Long userId);

    Optional<Expense> findByExpenseIdAndUser_Id(Long expenseId, Long userId);

    long countByUser_Id(Long userId);

    List<Expense> findByUser_IdAndExpenseDateBetweenOrderByExpenseDateDesc(Long userId,Date startDate,Date endDate);
}
