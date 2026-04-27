package com.ashuu.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

	private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

	// 🔥 relaxed API limit (fixes 429 spam)
	private final Bandwidth apiLimit = Bandwidth.simple(300, Duration.ofMinutes(1));

	// 🔥 stricter auth limit (login protection)
	private final Bandwidth authLimit = Bandwidth.simple(20, Duration.ofMinutes(1));

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String uri = request.getRequestURI();

		// 🚨 EXCLUDE IMPORTANT ENDPOINTS
		if (isExcluded(uri)) {
			filterChain.doFilter(request, response);
			return;
		}

		String key = resolveKey(request, uri);

		Bucket bucket = cache.computeIfAbsent(key, k -> createBucket(uri));

		ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

		if (probe.isConsumed()) {

			response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));

			filterChain.doFilter(request, response);

		} else {

			log.warn("🚨 Rate limit exceeded: {}", key);

			response.setStatus(429);
			response.setContentType("application/json");

			response.getWriter().write("""
					    {
					      "error": "Too many requests",
					      "message": "Rate limit exceeded. Please slow down."
					    }
					""");
		}
	}

	// ─────────────────────────────
	// 🚫 EXCLUDED ENDPOINTS
	// ─────────────────────────────
	private boolean isExcluded(String uri) {
		return uri.startsWith("/ws") || uri.startsWith("/uploads") || uri.startsWith("/actuator")
				|| uri.contains("swagger") || uri.startsWith("/v3/api-docs");
	}

	// ─────────────────────────────
	// 🔑 SMART KEY (fixes global blocking issue)
	// ─────────────────────────────
	private String resolveKey(HttpServletRequest request, String uri) {

		var auth = request.getUserPrincipal();

		String ip = getClientIP(request);

		if (auth != null) {
			return "USER_" + auth.getName() + "_" + uri;
		}

		return "IP_" + ip + "_" + uri;
	}

	private String getClientIP(HttpServletRequest request) {
		String xf = request.getHeader("X-Forwarded-For");

		if (xf != null && !xf.isBlank()) {
			return xf.split(",")[0];
		}

		return request.getRemoteAddr();
	}

	// ─────────────────────────────
	// 🪣 BUCKET CREATION
	// ─────────────────────────────
	private Bucket createBucket(String uri) {

		if (uri.startsWith("/api/auth")) {
			return Bucket.builder().addLimit(authLimit).build();
		}

		return Bucket.builder().addLimit(apiLimit).build();
	}
}