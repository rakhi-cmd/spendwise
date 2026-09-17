package com.expensetracker.spendwise.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.expensetracker.spendwise.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
	
	Optional <User> findByEmailOrPhone(String email, String phone);
	
	User findByPhoneOrEmail(String phone, String email);
	User findByEmailOrPhoneAndPassword(String email,String phone,String password);
    User findByPasswordAndEmailOrPhone(String password,String email,String phone);
    User findById(long userid);	
	User findByPhone(String phone);
	@SuppressWarnings("rawtypes")
	List<User> findByRoles(List roles);

	boolean existsByUserName(String username);
}
