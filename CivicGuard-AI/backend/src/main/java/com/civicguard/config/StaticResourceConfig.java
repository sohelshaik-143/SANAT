package com.civicguard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration to expose the local 'uploads' directory as a static resource.
 * This allows the frontend to retrieve citizen-submitted photos and officer 
 * resolution proofs via a standard URL (e.g., /api/uploads/filename.jpg).
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${complaint.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir);
        String uploadAbsolutePath = uploadPath.toFile().getAbsolutePath();
        
        // Ensure path ends with a slash for the resource handler
        if (!uploadAbsolutePath.endsWith("/") && !uploadAbsolutePath.endsWith("\\")) {
            uploadAbsolutePath += "/";
        }

        // Map /api/uploads/** to the physical directory
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations("file:/" + uploadAbsolutePath);
    }
}
