package com.shopme.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.shopme.security.oauth.CustomerOAuth2UserService;
import com.shopme.security.oauth.OAuth2LoginSuccessHandler;

@Configuration
public class WebSecurityConfig {

	@Autowired
	private CustomerOAuth2UserService oAuth2UserService;

	@Autowired
	private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

	@Autowired
	private DatabaseLoginSuccessHandler databaseLoginSuccessHandler;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain configureHttp(HttpSecurity http) throws Exception {
		// http.authorizeHttpRequests(
		// auth -> auth
		// .anyRequest()
		// .permitAll());
		// ...
		http.authorizeHttpRequests(
				auth -> auth
						.requestMatchers("/oauth2/**").permitAll()
						.requestMatchers("/account_details", "/update_account_details", "/orders/**",
								"/cart", "/address_book/**", "/checkout", "/place_order",
								"/process_paypal_order")
						.authenticated().anyRequest().permitAll())
				.formLogin(form -> form
						.loginPage("/login")
						.usernameParameter("email")
						.successHandler(databaseLoginSuccessHandler)
						.permitAll())
				.oauth2Login(oauth2 -> oauth2
						.loginPage("/login")
						.userInfoEndpoint(u -> u
								.userService(oAuth2UserService))
						.successHandler(oAuth2LoginSuccessHandler))
				.logout(logout -> logout.permitAll())
				.rememberMe(rem -> rem
						.key("AbcDefgHijKlmnOpqrs_1234567890")
						.tokenValiditySeconds(7 * 24 * 60 * 60))
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));

		return http.build();
	}

	@Bean
	WebSecurityCustomizer configureWebSecurity() throws Exception {
		return (web) -> web.ignoring().requestMatchers("/images/**", "/js/**", "/webjars/**");
	}

	@Bean
	UserDetailsService userDetailsService() {
		return new CustomerUserDetailsService();
	}

	@Bean
	DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}
}
