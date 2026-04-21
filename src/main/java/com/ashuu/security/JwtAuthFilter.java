package com.ashuu.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Fix 1: @Component — Spring manages this bean, constructor injection works correctly
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

	// ── Fix 6: shouldNotFilter() is the correct Spring hook for skipping ──────
	// Keeps skip logic out of doFilterInternal and avoids duplication with
	// SecurityConfig
    @Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		String method = request.getMethod();

		return path.startsWith("/api/auth/") // auth endpoints — no token needed
				|| path.startsWith("/uploads/") // static files — public
				|| "OPTIONS".equalsIgnoreCase(method);// CORS preflight
	}

	@Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
				// Fix 3: validate and extract in one block — single parse operation
				if (jwtUtil.isTokenValid(token)) {
					String username = jwtUtil.extractUsername(token);
					String role = jwtUtil.extractRole(token);
					System.out.println("ROLE FROM TOKEN = " + role);

					// Fix 4: null check before setting — not after
					if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

						// Fix 2: prefix role with ROLE_ so hasRole("ADMIN") works
						String prefixedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

						UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username,
								null, List.of(new SimpleGrantedAuthority(prefixedRole)));

						// Fix 5: attach request details (IP, session) to the token
						auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

						SecurityContextHolder.getContext().setAuthentication(auth);
					}
                }
            } catch (Exception ex) {
				// Invalid or tampered token — clear context and continue
				// Request will be rejected by SecurityConfig authorization rules
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}