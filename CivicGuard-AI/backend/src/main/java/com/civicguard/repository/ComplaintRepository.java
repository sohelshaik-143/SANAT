package com.civicguard.repository;

import com.civicguard.model.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Complaint documents.
 * Provides custom queries for dashboard analytics, escalation,
 * area-based reporting, and officer performance tracking.
 */
@Repository
public interface ComplaintRepository extends MongoRepository<Complaint, String> {

    // ─── Basic Lookups ────────────────────────────────────────

    Optional<Complaint> findByTicketNumber(String ticketNumber);

    List<Complaint> findByCitizenId(String citizenId);

    Page<Complaint> findByCitizenId(String citizenId, Pageable pageable);

    // ─── Status-based Queries ─────────────────────────────────

    List<Complaint> findByStatus(String status);

    List<Complaint> findByStatusIn(List<String> statuses);

    long countByStatus(String status);

    // ─── Officer Assignment ───────────────────────────────────

    List<Complaint> findByAssignedOfficerId(String officerId);

    Page<Complaint> findByAssignedOfficerIdAndStatus(
        String officerId, String status, Pageable pageable);

    long countByAssignedOfficerIdAndStatus(String officerId, String status);

    // ─── Area-based Queries ───────────────────────────────────

    List<Complaint> findByPincode(String pincode);

    List<Complaint> findByDistrictAndState(String district, String state);

    Page<Complaint> findByPincodeAndStatus(String pincode, String status, Pageable pageable);

    // ─── Department Queries ───────────────────────────────────

    List<Complaint> findByAssignedDepartment(String department);

    long countByAssignedDepartmentAndStatus(String department, String status);

    // ─── Escalation Queries ───────────────────────────────────

    /**
     * Find complaints that have exceeded their deadline and are not yet resolved.
     * Used by the AutoEscalationService scheduler.
     */
    @Query("{ 'deadline': { $lt: ?0 }, 'status': { $nin: ['RESOLVED', 'REJECTED'] }, 'escalationLevel': { $lt: 4 } }")
    List<Complaint> findOverdueComplaints(LocalDateTime now);

    /**
     * Find complaints at a specific escalation level.
     */
    List<Complaint> findByEscalationLevelAndStatusNot(int level, String status);

    // ─── Fraud Detection ──────────────────────────────────────

    List<Complaint> findByFraudDetectedTrue();

    @Query("{ 'assignedOfficerId': ?0, 'fraudDetected': true }")
    List<Complaint> findFraudByOfficer(String officerId);

    // ─── Analytics & Dashboard ────────────────────────────────

    /**
     * Count complaints by category for pie chart / analytics.
     */
    @Aggregation(pipeline = {
        "{ $group: { _id: '$category', count: { $sum: 1 } } }",
        "{ $sort: { count: -1 } }"
    })
    List<CategoryCount> countByCategory();

    /**
     * Count complaints by severity.
     */
    @Aggregation(pipeline = {
        "{ $group: { _id: '$severity', count: { $sum: 1 } } }",
        "{ $sort: { count: -1 } }"
    })
    List<CategoryCount> countBySeverity();

    /**
     * Heatmap data: count complaints per pincode.
     */
    @Aggregation(pipeline = {
        "{ $group: { _id: '$pincode', count: { $sum: 1 }, avgLat: { $avg: { $arrayElemAt: ['$location', 1] } }, avgLng: { $avg: { $arrayElemAt: ['$location', 0] } } } }",
        "{ $sort: { count: -1 } }",
        "{ $limit: 100 }"
    })
    List<HeatmapPoint> getHeatmapData();

    /**
     * Complaints submitted within a date range.
     */
    @Query("{ 'createdAt': { $gte: ?0, $lte: ?1 } }")
    List<Complaint> findByDateRange(LocalDateTime start, LocalDateTime end);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // ─── Projection Interfaces ────────────────────────────────

    interface CategoryCount {
        String getId();
        long getCount();
    }

    interface HeatmapPoint {
        String getId();
        long getCount();
        double getAvgLat();
        double getAvgLng();
    }
}
