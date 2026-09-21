package com.expensetracker.spendwise.controller;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class UnauthenticatedRequestHandler implements AuthenticationEntryPoint{

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		System.out.println("Request coming here !!!! "+request.getServletPath());
		if (request.getServletPath().startsWith("/admin/")) {
			response.sendRedirect("/admin/login");
		} else {
			response.sendRedirect("/");
		}
		
	}

}
