package com.expensetracker.spendwise.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExpenseReportDto {
 
	private Long id;
	private String title;
	private String date;
	private String category;
	private String subcategory;
	private String paymentMethod;
	private BigDecimal amount;
 
	public ExpenseReportDto(Long id, String title, String date, String category, String subcategory,
			String paymentMethod, BigDecimal amount) {
		this.id = id;
		this.title = title;
		this.date = date;
		this.category = category;
		this.subcategory = subcategory;
		this.paymentMethod = paymentMethod;
		this.amount = amount;
	}
}
