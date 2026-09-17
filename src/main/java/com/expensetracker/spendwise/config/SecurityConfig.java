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
	 *  Main Spring Security configuration. 
	 */
	
	private final CustomUserDetailsService userDetailsService;
	
	public SecurityConfig(CustomUserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
    	
    	http
        /*
         * Tell Spring Security to load users
         * using our database-backed UserDetailsService.
         */
        .userDetailsService(userDetailsService)

        /*
         * Session-based authentication for Thymeleaf.
         */
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1)
            .maxSessionsPreventsLogin(false)
        )

        /*
         * Authorization rules.
         */
        .authorizeHttpRequests(auth -> auth

            /*
             * Public pages
             */
            .requestMatchers(
                "/",
                "/index",
                "/login",
                "/signup",
                "/dashboard"
            ).permitAll()

            /*
             * Static resources
             */
            .requestMatchers(
            		"/static/**",
                "/css/**",
                "/js/**",
                "/images/**",
                "/webjars/**"
            ).permitAll()

            /*
             * Admin area
             */
            .requestMatchers("/admin/**")
            .hasRole("ADMIN")

            /*
             * REST APIs.
             *
             * For now they require authentication.
             * We can add JWT later.
             */
            .requestMatchers("/api/**")
            .authenticated()

            /*
             * Everything else requires authentication.
             */
            .anyRequest()
            .authenticated()
        )

        /*
         * Custom login page.
         */
        .formLogin(form -> form

            .loginPage("/login")

            /*
             * Spring Security processes this URL.
             * We don't need a controller for POST /login.
             */
            .loginProcessingUrl("/login")

            /*
             * Successful login.
             */
            .defaultSuccessUrl("/dashboard", true)

            /*
             * Failed login.
             */
            .failureUrl("/login?error=true")

            .permitAll()
        )

        /*
         * Logout configuration.
         */
        .logout(logout -> logout

            .logoutUrl("/logout")

            .logoutSuccessUrl("/login?logout=true")

            /*
             * Invalidate current HTTP session.
             */
            .invalidateHttpSession(true)

            /*
             * Remove authentication information.
             */
            .clearAuthentication(true)

            /*
             * Remove session cookie.
             */
            .deleteCookies("JSESSIONID")

            .permitAll()
        )

        /*
         * What happens when an unauthenticated user
         * tries to access a protected page?
         */
        .exceptionHandling(exception -> exception

            .authenticationEntryPoint(
                (request, response, authException) -> {
                    response.sendRedirect(
                        request.getContextPath() + "/login"
                    );
                }
            )

            /*
             * Authenticated but insufficient privileges.
             */
            .accessDeniedHandler(
                (request, response, accessDeniedException) -> {
                    response.sendRedirect(
                        request.getContextPath() + "/403"
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
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {

        return configuration.getAuthenticationManager();
    }

}
