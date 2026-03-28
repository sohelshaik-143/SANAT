package com.civicguard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * Configuration for the AI Verification Engine connection.
 * Sets up WebClient to communicate with the Python AI microservice
 * for image verification, deepfake detection, and issue classification.
 */
@Configuration
public class ImageAIConfig {

    @Value("${ai.engine.url:http://localhost:5000}")
    private String aiEngineUrl;

    @Value("${ai.engine.timeout:30}")
    private int timeoutSeconds;

    @Value("${ai.confidence.threshold:0.85}")
    private double confidenceThreshold;

    /**
     * WebClient bean configured for AI Engine communication.
     * Includes timeout settings and memory limits for large image payloads.
     */
    @Bean(name = "aiEngineWebClient")
    public WebClient aiEngineWebClient() {
        return WebClient.builder()
            .baseUrl(aiEngineUrl)
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(16 * 1024 * 1024)) // 16MB for image uploads
            .build();
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    /**
     * Issue categories recognized by the AI classifier.
     * Maps to Indian civic departments for auto-routing.
     */
    public enum IssueCategory {
        POTHOLE("Pothole / Road Damage", "PWD"),
        GARBAGE("Garbage Dumping", "Municipal Sanitation"),
        DRAINAGE("Drainage Overflow", "Municipal Water"),
        WATER_LEAK("Water Leakage", "Water Board"),
        STREETLIGHT("Broken Streetlight", "Municipal Electricity"),
        SEWAGE("Sewage Overflow", "Municipal Sanitation"),
        ENCROACHMENT("Illegal Encroachment", "Municipal Corporation"),
        TREE_FALL("Fallen Tree / Hazard", "Municipal Corporation"),
        ROAD_SIGN("Damaged Road Sign", "Traffic Police / NHAI"),
        BRIDGE_DAMAGE("Bridge / Flyover Damage", "PWD / NHAI"),
        CONSTRUCTION_DEBRIS("Construction Debris", "Municipal Corporation"),
        STRAY_ANIMALS("Stray Animal Menace", "Animal Husbandry"),
        NOISE_POLLUTION("Noise Pollution", "Pollution Control Board"),
        AIR_POLLUTION("Air Pollution / Burning", "Pollution Control Board"),
        OTHER("Other Civic Issue", "Municipal Corporation");

        private final String displayName;
        private final String department;

        IssueCategory(String displayName, String department) {
            this.displayName = displayName;
            this.department = department;
        }

        public String getDisplayName() { return displayName; }
        public String getDepartment() { return department; }
    }
}
