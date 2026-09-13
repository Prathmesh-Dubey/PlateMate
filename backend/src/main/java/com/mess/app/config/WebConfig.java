package com.mess.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:*", "http://127.0.0.1:*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadUri = Paths.get("uploads/stock-images").toAbsolutePath().toUri().toString();
        if (!uploadUri.endsWith("/")) {
            uploadUri += "/";
        }
        registry.addResourceHandler("/uploads/stock-images/**")
                .addResourceLocations(uploadUri);

        String rootUploadUri = Paths.get("uploads").toAbsolutePath().toUri().toString();
        if (!rootUploadUri.endsWith("/")) {
            rootUploadUri += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(rootUploadUri);
    }
}
