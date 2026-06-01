package com.example.ems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	private final CustomPermissionEvaluator customPermissionEvaluator;

	public SecurityConfig(CustomPermissionEvaluator customPermissionEvaluator) {
		this.customPermissionEvaluator = customPermissionEvaluator;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
			http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/css/**", "/js/**", "/images/**", "/auth/**", "/login-required", "/test-swap-request.html", "/test-swap-simple", "/test-create-swap", "/permission-test/**").authenticated()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.requestMatchers("/hr/**").hasAnyRole("HR", "ADMIN")
				.requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")
					// Allow public access to all attendance endpoints
					.requestMatchers("/attendance/**").permitAll()
					.requestMatchers(HttpMethod.GET, "/leave/balances", "/leave/balances/**").authenticated()
					.requestMatchers("/attendance/**").hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
					.requestMatchers("/leave/**").authenticated()
					.requestMatchers("/approvals/**").hasAnyRole("MANAGER", "HR", "ADMIN")
					.requestMatchers("/reports/**").hasAnyRole("MANAGER", "HR", "ADMIN")
					.requestMatchers("/performance/**").hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
					.requestMatchers("/shifts/**").authenticated()
					.requestMatchers("/profile/**").hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
					.requestMatchers("/notifications/settings/**").authenticated()
					.requestMatchers("/notifications/**").hasAnyRole("EMPLOYEE", "MANAGER", "HR", "ADMIN")
					.requestMatchers("/alerts/**").hasAnyRole("MANAGER", "HR", "ADMIN")
					// Restrict employee management to HR and Admin
					.requestMatchers("/employee/**").hasAnyRole("HR", "ADMIN")
					// Require authentication for root path
					.requestMatchers("/").authenticated()
				.anyRequest().authenticated()
			)
				.formLogin(form -> form
					.loginPage("/login")
					.defaultSuccessUrl("/", true)
					.failureUrl("/login?error=true")
					.permitAll()
				)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login?logout=true")
				.invalidateHttpSession(true)
				.clearAuthentication(true)
				.deleteCookies("JSESSIONID")
				.permitAll()
				.logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/logout", "GET"))
			);

		http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
		http.csrf(csrf -> csrf.ignoringRequestMatchers("/attendance/**", "/logout"));
		http.exceptionHandling(eh -> eh.accessDeniedPage("/access-denied"));
		http.sessionManagement(sm -> sm
			.maximumSessions(1)
			.maxSessionsPreventsLogin(false)
			.sessionRegistry(sessionRegistry())
		);
		http.sessionManagement(sm -> sm
			.sessionFixation(sf -> sf.migrateSession())
			.invalidSessionUrl("/login")
			.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
			.sessionConcurrency(sc -> sc.maximumSessions(1).expiredUrl("/login"))
		);
		return http.build();
	}

	@Bean
	public UserDetailsService users(PasswordEncoder encoder) {
		return new InMemoryUserDetailsManager(
			User.withUsername("admin").password(encoder.encode("admin123")).roles("ADMIN").build(),
			User.withUsername("hr").password(encoder.encode("hr123")).roles("HR").build(),
			User.withUsername("manager").password(encoder.encode("manager123")).roles("MANAGER").build(),
			User.withUsername("employee").password(encoder.encode("emp123")).roles("EMPLOYEE").build(),
			// Additional sample users
			User.withUsername("dinaya").password(encoder.encode("dinaya123")).roles("ADMIN").build(),
			User.withUsername("sadeni").password(encoder.encode("sadeni123")).roles("HR").build(),
			User.withUsername("nethmi").password(encoder.encode("nethmi123")).roles("MANAGER").build(),
			User.withUsername("tharushi").password(encoder.encode("tharushi123")).roles("EMPLOYEE").build(),
			User.withUsername("hiruni").password(encoder.encode("hiruni123")).roles("EMPLOYEE").build()
		);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public SessionRegistry sessionRegistry() {
		return new SessionRegistryImpl();
	}

	@Bean
	public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
		DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
		handler.setPermissionEvaluator(customPermissionEvaluator);
		return handler;
	}
}


