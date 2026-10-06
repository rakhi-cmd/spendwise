package com.expensetracker.spendwise.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subcategories")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubCategory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long subCategoryId;
	
	@Column
	private String name;
	
	@Column
	private String description;
	
	@ManyToOne
	@JoinColumn(name = "categoryid")
	private Category category;
	
	@Builder.Default
	private int status = 1;
	
	@ManyToOne
	@JoinColumn(name = "created_by")
	@JsonIgnore
	private User createdBy;

	@Builder.Default
	@Column(updatable=false)
	@JsonFormat(shape=JsonFormat.Shape.STRING,pattern="yyyy-MM-dd hh:mm:ss")
	private Date createdDateTime=new Date();

	@ManyToOne
	@JoinColumn(name = "modified_by")
	@JsonIgnore
	private User modifiedBy;

	@Column(updatable=false)
	@JsonFormat(shape=JsonFormat.Shape.STRING,pattern="yyyy-MM-dd hh:mm:ss")
	private Date modifiedDateTime;
}
