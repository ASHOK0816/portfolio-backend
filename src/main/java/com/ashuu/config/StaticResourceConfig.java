package com.ashuu.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

	// Fix 6: matches the same property used by FileStorageService
	@Value("${file.upload.base-dir:${user.dir}/uploads}")
	private String baseDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Ensure trailing slash so Spring resolves sub-paths correctly
		String location = "file:" + baseDir + (baseDir.endsWith("/") ? "" : "/");

		registry.addResourceHandler("/uploads/**").addResourceLocations(location);
	}
}