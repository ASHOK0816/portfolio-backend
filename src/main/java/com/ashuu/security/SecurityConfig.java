package com.ashuu.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

	private final JwtUtil jwtUtil;
	private final JwtAuthFilter jwtAuthFilter;

	// Fix 2: allowed origins from config — never hardcode in source
	@Value("${app.cors.allowed-origins=http://localhost:5173,http://localhost:3000,http://localhost:3001}")
	private List<String> allowedOrigins;

	// Fix 4: JwtAuthFilter injected by Spring, not newed manually
	public SecurityConfig(JwtUtil jwtUtil, JwtAuthFilter jwtAuthFilter) {
		this.jwtUtil = jwtUtil;
		this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

	// ── Fix 2: explicit CORS bean ─────────────────────────────────────────────
    @Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		// Only allow your frontend origin(s) — not wildcard
		config.setAllowedOrigins(allowedOrigins);
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Signup-Secret" // required for signup
																							// endpoint
		));
		config.setExposedHeaders(List.of("Authorization"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L); // cache preflight for 1 hour

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
				// Fix 2: pass the explicit CORS config bean
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
				.formLogin(form -> form.disable()).httpBasic(basic -> basic.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth

						// ── CORS preflight ────────────────────────────────────────────
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

						// ── WebSocket ─────────────────────────────────────────────────
						.requestMatchers("/ws/**", "/ws/info/**").permitAll()

						// ── Static uploads ────────────────────────────────────────────
						.requestMatchers("/uploads/**").permitAll()

						// ── Auth — all open (signup guarded by X-Signup-Secret header)
						.requestMatchers("/api/auth/**").permitAll()

						// ── Public GET APIs ───────────────────────────────────────────
						.requestMatchers(HttpMethod.GET, "/api/projects/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/skills/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/education/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/resume/download/**").permitAll()

						// Fix 3: removed redundant /api/profile — /** covers it
						.requestMatchers(HttpMethod.GET, "/api/profile/**").permitAll()

						// Fix 6: only the contact POST is public — not all GET /messages
						.requestMatchers(HttpMethod.POST, "/api/messages/contact").permitAll()

						// ── Admin-only ────────────────────────────────────────────────
						// 🔐 Only admin-specific APIs protected
						.requestMatchers("/api/admin/**").hasRole("ADMIN")

						.anyRequest().authenticated())
				// Fix 4: use Spring-managed filter instance
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

				// Fix 5: logout endpoint that invalidates the refresh token
				.logout(logout -> logout.logoutUrl("/api/auth/logout").addLogoutHandler(logoutHandler())
						.logoutSuccessHandler((req, res, auth) -> res.setStatus(200))
            );

        return http.build();
    }

	// ── Fix 5: logout handler clears refresh token from DB ───────────────────
	private org.springframework.security.web.authentication.logout.LogoutHandler logoutHandler() {

		return (request, response, authentication) -> {
			String authHeader = request.getHeader("Authorization");
			if (authHeader == null || !authHeader.startsWith("Bearer "))
				return;

			String token = authHeader.substring(7);
			String username = jwtUtil.extractUsername(token);
			if (username != null) {
				// RefreshTokenService.deleteByAdmin() cleans up the DB row
				// We access it via the application context to avoid circular deps
				org.springframework.web.context.support.WebApplicationContextUtils
						.getWebApplicationContext(request.getServletContext())
						.getBean(com.ashuu.service.RefreshTokenService.class).deleteByAdmin(
								// Load admin inline — lightweight lookup
								new com.ashuu.model.Admin() {
									{
										setUsername(username);
									}
								});
			}
		};
	}
}