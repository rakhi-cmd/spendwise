package com.expensetracker.spendwise.response;

public interface ResponseDao {
	
	void setResponse(Object response);
	
	Object getResponse();
	
	void setStatusCode(int statusCode);
	
	int getStatusCode();
	
	void setStatus(String status);
	
	String getStatus();
	
	void setResponseMessage(String responseMessage);
	
	String getResponseMessage();

}
