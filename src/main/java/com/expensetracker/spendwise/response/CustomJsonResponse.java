package com.expensetracker.spendwise.response;

import lombok.Data;

@Data
public class CustomJsonResponse implements ResponseDao{
	
	private Object response;
	private String responseMessage;
	private int statusCode;
	private String status;
	
	

	public CustomJsonResponse(Object response, String responseMessage, int statusCode, String status) {
		this.response = response;
		this.responseMessage = responseMessage;
		this.statusCode = statusCode;
		this.status = status;
	}

}
