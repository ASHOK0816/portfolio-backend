package com.ashuu.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	// ── WebSocket endpoint ─────────────────────────────
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {

		registry.addEndpoint("/ws").setAllowedOriginPatterns("http://localhost:3000") // ✅ restrict frontend
				.withSockJS();
	}

	// ── Message broker ─────────────────────────────
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {

		// For subscriptions
		registry.enableSimpleBroker("/topic", "/queue");

		// For sending messages
		registry.setApplicationDestinationPrefixes("/app");

		// ✅ For user-specific messaging (VERY IMPORTANT)
		registry.setUserDestinationPrefix("/user");
	}
}