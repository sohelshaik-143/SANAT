package com.civicguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CivicGuard-AI: AI-Powered Civic Issue Monitoring & Auto-Escalation System
 * 
 * Main application entry point for the Spring Boot backend service.
 * Enables MongoDB auditing for automatic timestamp management and
 * scheduling for the auto-escalation cron jobs.
 * 
 * @author CivicGuard Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableMongoAuditing
@EnableScheduling
public class CivicGuardApplication {

    public static void main(String[] args) {
        SpringApplication.run(CivicGuardApplication.class, args);
        System.out.println("""
            
            ╔══════════════════════════════════════════════════════╗
            ║        🛡️  CivicGuard-AI Server Started  🛡️        ║
            ║   AI-Powered Civic Issue Monitoring & Escalation    ║
            ║          Built for Digital India 🇮🇳                 ║
            ╚══════════════════════════════════════════════════════╝
            
            → API:        http://localhost:8080/api
            → Health:     http://localhost:8080/actuator/health
            → Swagger:    http://localhost:8080/swagger-ui.html
            
        """);
    }
}
