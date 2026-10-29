package com.expensetracker.spendwise.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.expensetracker.spendwise.model.User;

public interface UserRepository extends JpaRepository<User, Long>{
	
	Optional <User> findByEmailOrPhone(String email, String phone);
	
	//User findByPhoneOrEmail(String phone, String email);
	//User findByEmailOrPhoneAndPassword(String email,String phone,String password);
    //User findByPasswordAndEmailOrPhone(String password,String email,String phone);
    User findById(long userid);	
	User findByPhone(String phone);
	@SuppressWarnings("rawtypes")
	List<User> findByRoles(List roles);

	boolean existsByUserName(String username);
	
	User findByEmail(String email);

	boolean existsByEmail(String email);
	
	@Query("SELECT COUNT(DISTINCT u.id) FROM User u JOIN u.roles r WHERE u.status = :status AND r = :role")
	long countActiveUsersByRole(@Param("status") int status, @Param("role") String role);

	User findByUserName(String userName);
}
