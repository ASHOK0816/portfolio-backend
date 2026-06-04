package com.ashuu.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ashuu.service.AdminDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	private final RateLimitingFilter rateLimitingFilter;
	private final AdminDetailsService userDetailsService;
	private final CustomAuthenticationEntryPoint authEntryPoint;
	private final CustomAccessDeniedHandler accessDeniedHandler;

	private static final String[] PUBLIC = { "/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/actuator/health" };

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
				// 🔥 CORS
				.cors(cors -> cors.configurationSource(corsConfig()))

				// 🔥 Disable CSRF for APIs
				.csrf(csrf -> csrf.disable())

				// 🔥 Stateless session (JWT)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// 🔥 Authorization rules
				.authorizeHttpRequests(auth -> auth.requestMatchers(PUBLIC).permitAll()

						.requestMatchers("/ws/**").permitAll()
						.requestMatchers("/ws/info/**").permitAll().requestMatchers("/ws/info").permitAll()
						.requestMatchers("/uploads/**").permitAll()

						// ✅ Allow PDF preview without token
						.requestMatchers(HttpMethod.GET, "/api/resume/view/**").permitAll()

						// GET → USER + ADMIN
						.requestMatchers(HttpMethod.GET, "/api/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

						// WRITE → ADMIN ONLY
						.requestMatchers(HttpMethod.POST, "/api/**").hasAuthority("ROLE_ADMIN")

						.requestMatchers(HttpMethod.PUT, "/api/**").hasAuthority("ROLE_ADMIN")

						.requestMatchers(HttpMethod.DELETE, "/api/**").hasAuthority("ROLE_ADMIN")

						.anyRequest().authenticated())

				// 🔥 Exception handling
				.exceptionHandling(
						ex -> ex.authenticationEntryPoint(authEntryPoint).accessDeniedHandler(accessDeniedHandler))

				// 🔥 FILTER ORDER (FIXED)
				.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

				// 🔥 Authentication provider
				.authenticationProvider(authenticationProvider());

		return http.build();
	}

	// ── AUTH PROVIDER ─────────────────────────────

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	// ── PASSWORD ─────────────────────────────────

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	// ── CORS CONFIG ──────────────────────────────

	@Bean
	public CorsConfigurationSource corsConfig() {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOrigins(List.of("http://localhost:3000"));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return source;
	}
}