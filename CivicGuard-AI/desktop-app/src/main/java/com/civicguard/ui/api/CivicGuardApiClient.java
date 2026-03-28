package com.civicguard.ui.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * REST API client for the CivicGuard-AI backend.
 * Used by the JavaFX desktop app to communicate with the Spring Boot server.
 */
public class CivicGuardApiClient {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080/api";
    private final String baseUrl;
    private final HttpClient httpClient;
    private String authToken;

    public CivicGuardApiClient() {
        this(DEFAULT_BASE_URL);
    }

    public CivicGuardApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    /**
     * Authenticate officer and store JWT token.
     */
    public String login(String email, String password) throws IOException, InterruptedException {
        String json = String.format(
            "{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/auth/login"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            // Parse token from response (simplified — use JSON parser in production)
            String body = response.body();
            int tokenStart = body.indexOf("\"token\":\"") + 9;
            int tokenEnd = body.indexOf("\"", tokenStart);
            this.authToken = body.substring(tokenStart, tokenEnd);
            return this.authToken;
        }

        throw new IOException("Login failed: " + response.statusCode());
    }

    /**
     * Get assigned complaints for the logged-in officer.
     */
    public String getAssignedComplaints(String status, int page, int size)
            throws IOException, InterruptedException {
        return authenticatedGet(String.format(
            "/complaints/assigned?status=%s&page=%d&size=%d", status, page, size));
    }

    /**
     * Get complaint details by ID.
     */
    public String getComplaint(String id) throws IOException, InterruptedException {
        return authenticatedGet("/complaints/" + id);
    }

    /**
     * Get dashboard statistics.
     */
    public String getDashboardStats() throws IOException, InterruptedException {
        return authenticatedGet("/dashboard/stats");
    }

    /**
     * Get heatmap data.
     */
    public String getHeatmapData() throws IOException, InterruptedException {
        return authenticatedGet("/dashboard/heatmap");
    }

    /**
     * Perform authenticated GET request.
     */
    private String authenticatedGet(String endpoint) throws IOException, InterruptedException {
        if (authToken == null) {
            throw new IOException("Not authenticated. Please login first.");
        }

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + endpoint))
            .header("Authorization", "Bearer " + authToken)
            .header("Content-Type", "application/json")
            .GET()
            .build();

        HttpResponse<String> response = httpClient.send(request,
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        }

        throw new IOException("API error: " + response.statusCode() + " " + response.body());
    }

    public boolean isAuthenticated() {
        return authToken != null;
    }

    public void logout() {
        this.authToken = null;
    }
}
