package com.civicguard.ui.controllers;

import com.civicguard.ui.api.CivicGuardApiClient;
import com.civicguard.ui.models.ComplaintModel;
import com.civicguard.ui.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller handling dashboard logic for the officer desktop app.
 * Fetches data from the Spring Boot API and transforms it for UI display.
 */
public class DashboardController {

    private final CivicGuardApiClient apiClient;

    public DashboardController(CivicGuardApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Fetch dashboard overview statistics from the backend.
     */
    public DashboardStats fetchDashboardStats() {
        try {
            String json = apiClient.getDashboardStats();
            // In production: use Jackson/Gson to parse JSON
            return parseDashboardStats(json);
        } catch (Exception e) {
            System.err.println("Failed to fetch dashboard stats: " + e.getMessage());
            return new DashboardStats(); // Return empty stats
        }
    }

    /**
     * Fetch complaints assigned to the current officer.
     */
    public List<ComplaintModel> fetchAssignedComplaints(String status) {
        try {
            String json = apiClient.getAssignedComplaints(status, 0, 50);
            return parseComplaints(json);
        } catch (Exception e) {
            System.err.println("Failed to fetch complaints: " + e.getMessage());
            UIUtils.showError("Connection Error",
                "Could not connect to CivicGuard server.\n" + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Login the officer.
     */
    public boolean login(String email, String password) {
        try {
            apiClient.login(email, password);
            return true;
        } catch (Exception e) {
            UIUtils.showError("Login Failed", e.getMessage());
            return false;
        }
    }

    // ─── Parsing helpers (simplified — use JSON library in production) ───

    private DashboardStats parseDashboardStats(String json) {
        DashboardStats stats = new DashboardStats();
        // Placeholder — parse JSON in production
        stats.totalComplaints = 47;
        stats.resolved = 38;
        stats.pending = 6;
        stats.escalated = 3;
        stats.fraudCases = 1;
        stats.resolutionRate = 80.85;
        return stats;
    }

    private List<ComplaintModel> parseComplaints(String json) {
        // Placeholder — parse JSON response in production
        return new ArrayList<>();
    }

    /**
     * Dashboard statistics container.
     */
    public static class DashboardStats {
        public long totalComplaints;
        public long resolved;
        public long pending;
        public long escalated;
        public long fraudCases;
        public double resolutionRate;
        public long complaintsToday;
        public long complaintsThisWeek;
    }
}
