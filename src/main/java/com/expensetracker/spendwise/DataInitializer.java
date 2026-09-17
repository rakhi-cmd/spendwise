package com.expensetracker.spendwise;

import java.util.Arrays;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.expensetracker.spendwise.model.User;
import com.expensetracker.spendwise.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner{
	
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Override
	public void run(String... args) throws Exception {
		
		if (!userRepo.existsByUserName("admin")) {

	        userRepo.save(
	            User.builder()
	                .userName("admin")
	                .password(passwordEncoder.encode("admin"))
	                .roles(Arrays.asList("ROLE_ADMIN"))
	                .createdDate(new Date())
	                .status(1)
	                .email("admin@gmail.com")
	                .phone("7428730894")
	                .build()
	        );
	    }
	}

}
