package com.expensetracker.spendwise.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.expensetracker.spendwise.repository.UserRepository;

@Component
public class CustomUserDetailsService implements UserDetailsService{
	
	private final UserRepository userRepository;
	
	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
		
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		return this.userRepository.findByEmailOrPhone(username, username)
				.orElseThrow(() ->
                new UsernameNotFoundException(
                		"Username: " + username + " not found"
                    )
                );
	}

}
