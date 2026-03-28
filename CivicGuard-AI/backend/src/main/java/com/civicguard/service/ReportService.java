package com.civicguard.service;

import com.civicguard.repository.ComplaintRepository;
import com.civicguard.repository.OfficerRepository;
import com.civicguard.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating analytics, dashboard data, and reports.
 * Powers the government transparency dashboard with real-time metrics,
 * heatmaps, department performance, and officer efficiency tracking.
 */
@Service
public class ReportService {

    private final ComplaintRepository complaintRepo;
    private final OfficerRepository officerRepo;
    private final UserRepository userRepo;

    public ReportService(ComplaintRepository complaintRepo,
                         OfficerRepository officerRepo,
                         UserRepository userRepo) {
        this.complaintRepo = complaintRepo;
        this.officerRepo = officerRepo;
        this.userRepo = userRepo;
    }

    // ═══════════════════════════════════════════════════════════
    //  DASHBOARD OVERVIEW STATS
    // ═══════════════════════════════════════════════════════════

    /**
     * Get the main dashboard statistics overview.
     */
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // Total counts
        stats.put("totalComplaints", complaintRepo.count());
        stats.put("totalCitizens", userRepo.countByActiveTrue());
        stats.put("totalOfficers", officerRepo.count());

        // Status breakdown
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        statusCounts.put("submitted", complaintRepo.countByStatus("SUBMITTED"));
        statusCounts.put("verified", complaintRepo.countByStatus("VERIFIED"));
        statusCounts.put("assigned", complaintRepo.countByStatus("ASSIGNED"));
        statusCounts.put("inProgress", complaintRepo.countByStatus("IN_PROGRESS"));
        statusCounts.put("escalated", complaintRepo.countByStatus("ESCALATED"));
        statusCounts.put("resolved", complaintRepo.countByStatus("RESOLVED"));
        statusCounts.put("rejected", complaintRepo.countByStatus("REJECTED"));
        statusCounts.put("fraudDetected", complaintRepo.countByStatus("FRAUD_DETECTED"));
        stats.put("statusBreakdown", statusCounts);

        // Resolution rate
        long total = complaintRepo.count();
        long resolved = complaintRepo.countByStatus("RESOLVED");
        stats.put("resolutionRate", total > 0 ? (double) resolved / total * 100 : 0);

        // Fraud stats
        stats.put("totalFraudCases", complaintRepo.findByFraudDetectedTrue().size());
        stats.put("flaggedOfficers", officerRepo.findByFlaggedForReviewTrue().size());

        // Today's activity
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        stats.put("complaintsToday", complaintRepo.countByCreatedAtBetween(
            todayStart, LocalDateTime.now()));

        // This week
        LocalDateTime weekStart = todayStart.minusDays(7);
        stats.put("complaintsThisWeek", complaintRepo.countByCreatedAtBetween(
            weekStart, LocalDateTime.now()));

        return stats;
    }

    // ═══════════════════════════════════════════════════════════
    //  CATEGORY ANALYTICS
    // ═══════════════════════════════════════════════════════════

    /**
     * Get complaint distribution by category for charts.
     */
    public List<Map<String, Object>> getCategoryDistribution() {
        return complaintRepo.countByCategory().stream()
            .map(cc -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("category", cc.getId());
                item.put("count", cc.getCount());
                return item;
            })
            .collect(Collectors.toList());
    }

    /**
     * Get complaint distribution by severity.
     */
    public List<Map<String, Object>> getSeverityDistribution() {
        return complaintRepo.countBySeverity().stream()
            .map(sc -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("severity", sc.getId());
                item.put("count", sc.getCount());
                return item;
            })
            .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  HEATMAP DATA
    // ═══════════════════════════════════════════════════════════

    /**
     * Get geographical heatmap data — complaints clustered by pincode
     * with average coordinates for map visualization.
     */
    public List<Map<String, Object>> getHeatmapData() {
        return complaintRepo.getHeatmapData().stream()
            .map(hp -> {
                Map<String, Object> point = new LinkedHashMap<>();
                point.put("pincode", hp.getId());
                point.put("count", hp.getCount());
                point.put("latitude", hp.getAvgLat());
                point.put("longitude", hp.getAvgLng());
                // Intensity for heatmap rendering
                point.put("intensity", Math.min(1.0, hp.getCount() / 50.0));
                return point;
            })
            .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  DEPARTMENT PERFORMANCE
    // ═══════════════════════════════════════════════════════════

    /**
     * Get performance metrics for each department.
     */
    public List<Map<String, Object>> getDepartmentPerformance() {
        List<String> departments = List.of(
            "PWD", "Municipal Sanitation", "Water Board",
            "Municipal Electricity", "Municipal Corporation",
            "Pollution Control Board", "Animal Husbandry"
        );

        return departments.stream()
            .map(dept -> {
                Map<String, Object> metrics = new LinkedHashMap<>();
                metrics.put("department", dept);

                long assigned = complaintRepo.countByAssignedDepartmentAndStatus(dept, "ASSIGNED");
                long inProgress = complaintRepo.countByAssignedDepartmentAndStatus(dept, "IN_PROGRESS");
                long resolved = complaintRepo.countByAssignedDepartmentAndStatus(dept, "RESOLVED");
                long escalated = complaintRepo.countByAssignedDepartmentAndStatus(dept, "ESCALATED");
                long total = assigned + inProgress + resolved + escalated;

                metrics.put("totalComplaints", total);
                metrics.put("resolved", resolved);
                metrics.put("pending", assigned + inProgress);
                metrics.put("escalated", escalated);
                metrics.put("resolutionRate", total > 0 ? (double) resolved / total * 100 : 0);

                return metrics;
            })
            .sorted((a, b) -> Long.compare(
                (long) b.get("totalComplaints"), (long) a.get("totalComplaints")))
            .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  OFFICER PERFORMANCE
    // ═══════════════════════════════════════════════════════════

    /**
     * Get officer performance leaderboard.
     */
    public List<Map<String, Object>> getOfficerPerformance() {
        return officerRepo.findAll().stream()
            .filter(o -> o.getTotalAssigned() > 0)
            .map(officer -> {
                Map<String, Object> metrics = new LinkedHashMap<>();
                metrics.put("officerId", officer.getId());
                metrics.put("name", officer.getName());
                metrics.put("designation", officer.getDesignation());
                metrics.put("department", officer.getDepartment());
                metrics.put("district", officer.getDistrict());
                metrics.put("totalAssigned", officer.getTotalAssigned());
                metrics.put("totalResolved", officer.getTotalResolved());
                metrics.put("totalEscalated", officer.getTotalEscalated());
                metrics.put("fraudFlags", officer.getTotalFraudFlags());
                metrics.put("performanceScore", officer.getPerformanceScore());
                metrics.put("avgResolutionHours", officer.getAvgResolutionHours());
                metrics.put("flaggedForReview", officer.isFlaggedForReview());
                return metrics;
            })
            .sorted((a, b) -> Double.compare(
                (double) b.get("performanceScore"), (double) a.get("performanceScore")))
            .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  TREND DATA
    // ═══════════════════════════════════════════════════════════

    /**
     * Get daily complaint trend for the last 30 days.
     */
    public List<Map<String, Object>> getComplaintTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);

            long count = complaintRepo.countByCreatedAtBetween(dayStart, dayEnd);

            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", dayStart.toLocalDate().toString());
            point.put("count", count);
            trend.add(point);
        }

        return trend;
    }
}
