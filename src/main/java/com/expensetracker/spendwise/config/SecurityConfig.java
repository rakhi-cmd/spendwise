package com.expensetracker.spendwise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.expensetracker.spendwise.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {
	/*
	 * Main Spring Security configuration.
	 */

	private final CustomUserDetailsService userDetailsService;

	public SecurityConfig(CustomUserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
				/*
				 * Tell Spring Security to load users using our database-backed
				 * UserDetailsService.
				 */
				.userDetailsService(userDetailsService)

				/*
				 * Session-based authentication for Thymeleaf.
				 */
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
						.maximumSessions(1).maxSessionsPreventsLogin(false))

				/*
				 * Authorization rules.
				 */
				.authorizeHttpRequests(auth -> auth

						/*
						 * Public pages
						 */
						.requestMatchers("/", "/index", "/login", "/signup", "/dashboard","/403").permitAll()

						/*
						 * Static resources
						 */
						.requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

						/*
						 * Admin area
						 */
						.requestMatchers("/admin/**").hasRole("ADMIN")
						
						/*
						 * REST APIs.
						 *
						 * For now they require authentication. We can add JWT later.
						 */
						//.requestMatchers("/api/**").authenticated()

						/*
						 * Everything else requires authentication.
						 */
						.anyRequest().authenticated())

				/*
				 * Custom login page.
				 */
				.formLogin(form -> form

						.loginPage("/login")

						/*
						 * Spring Security processes this URL. We don't need a controller for POST
						 * /login.
						 */
						.loginProcessingUrl("/login")
						
						/*
		                 * Role-based dashboard redirect.
		                 *
		                 * ADMIN -> /admin/dashboard
		                 * USER  -> /dashboard
		                 */
		                .successHandler((request, response, authentication) -> {

		                    boolean isAdmin = authentication
		                        .getAuthorities()
		                        .stream()
		                        .anyMatch(authority ->
		                            authority.getAuthority()
		                                .equals("ROLE_ADMIN")
		                        );

		                    if (isAdmin) {

		                        response.sendRedirect(
		                            request.getContextPath()
		                                + "/admin/dashboard"
		                        );

		                    } else {

		                        response.sendRedirect(
		                            request.getContextPath()
		                                + "/dashboard"
		                        );
		                    }
		                })

						/*
						 * Failed login.
						 */
						.failureUrl("/login?error=true")

						.permitAll())

				/*
	             * =========================================================
	             * LOGOUT
	             * =========================================================
	             *
	             * Both ADMIN and USER use:
	             *
	             * POST /logout
	             */
	            .logout(logout -> logout

	                .logoutUrl("/logout")

	                .logoutSuccessUrl("/login?logout=true")

	                /*
	                 * Invalidate the current session.
	                 */
	                .invalidateHttpSession(true)

	                /*
	                 * Clear authentication.
	                 */
	                .clearAuthentication(true)

	                /*
	                 * Remove session cookie.
	                 */
	                .deleteCookies("JSESSIONID")

	                .permitAll()
	            )

	            /*
	             * =========================================================
	             * EXCEPTION HANDLING
	             * =========================================================
	             */
	            .exceptionHandling(exception -> exception
	                /*
	                 * User is NOT logged in.
	                 *
	                 * Redirect to login page.
	                 */
	                .authenticationEntryPoint(
	                    (request, response, authException) -> {

	                        response.sendRedirect(
	                            request.getContextPath()
	                                + "/login"
	                        );
	                    }
	                )
	                /*
	                 * User IS logged in but does not have
	                 * the required role.
	                 *
	                 * Example:
	                 *
	                 * USER tries:
	                 * /admin/dashboard
	                 *
	                 * -> /403
	                 */
	                .accessDeniedHandler(
	                    (request, response, accessDeniedException) -> {

	                        response.sendRedirect(
	                            request.getContextPath()
	                                + "/403"
	                        );
	                    }
	                )
	            );
		return http.build();

	}

	/*
	 * BCrypt password encryption.
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/*
	 * AuthenticationManager.
	 */
	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

		return configuration.getAuthenticationManager();
	}

}
