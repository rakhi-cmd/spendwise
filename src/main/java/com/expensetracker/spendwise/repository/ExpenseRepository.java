package com.expensetracker.spendwise.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.expensetracker.spendwise.dto.ExpenseReportDto;
import com.expensetracker.spendwise.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long>{
	
	List<Expense> findByUser_IdOrderByExpenseDateDesc(Long userId);

    Optional<Expense> findByExpenseIdAndUser_Id(Long expenseId, Long userId);

    long countByUser_Id(Long userId);

    List<Expense> findByUser_IdAndExpenseDateBetweenOrderByExpenseDateDesc(Long userId,Date startDate,Date endDate);
    
    @EntityGraph(attributePaths = {"category"})
    List<Expense> findAllByOrderByExpenseDateDesc();
    
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e")
	BigDecimal getTotalExpenses();
    
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.expenseDate >= :startDate AND e.expenseDate < :endDate ")
    BigDecimal getExpensesBetweenDates(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    @Query("SELECT e.category.name, SUM(e.amount) FROM Expense e WHERE e.expenseDate >= :startDate AND e.expenseDate < :endDate AND e.category IS NOT NULL GROUP BY e.category.name ORDER BY SUM(e.amount) DESC")
    List<Object[]> findCategoryTotalsBetweenDates(@Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

    @Query("""
    		select e from Expense e
    		join fetch e.category
    		left join fetch e.subcategory
    		where e.user.name = :username
    		AND e.status = 1
    		  and e.expenseDate >= :start
    		  and e.expenseDate < :end
    		order by e.expenseDate asc
    		""")
    List<Expense> findMonthlyExpenses(@Param("username") String username,
    		@Param("start") Date start,
    		@Param("end") Date end);
}
