package com.civicguard.repository;

import com.civicguard.model.Officer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MongoDB repository for Officer documents.
 * Supports escalation chain lookups and performance queries.
 */
@Repository
public interface OfficerRepository extends MongoRepository<Officer, String> {

    Optional<Officer> findByEmail(String email);

    Optional<Officer> findByPhone(String phone);

    Optional<Officer> findByEmployeeId(String employeeId);

    boolean existsByEmail(String email);

    // ─── Department & Jurisdiction ────────────────────────────

    List<Officer> findByDepartmentAndDistrictAndActiveTrue(String department, String district);

    List<Officer> findByDepartmentAndActiveTrue(String department);

    /**
     * Find officers by department and assigned pincodes for complaint routing.
     */
    @Query("{ 'department': ?0, 'assignedPincodes': ?1, 'active': true }")
    List<Officer> findByDepartmentAndPincode(String department, String pincode);

    // ─── Escalation Chain ─────────────────────────────────────

    /**
     * Find the next-tier officer for escalation.
     * E.g., from tier 1 (Field Officer) → tier 2 (Block Officer)
     */
    @Query("{ 'department': ?0, 'district': ?1, 'escalationTier': ?2, 'active': true }")
    List<Officer> findEscalationTarget(String department, String district, int nextTier);

    List<Officer> findByEscalationTierAndDepartmentAndActiveTrue(
        int tier, String department);

    // ─── Performance & Fraud ──────────────────────────────────

    List<Officer> findByFlaggedForReviewTrue();

    @Query("{ 'performanceScore': { $lt: ?0 }, 'active': true }")
    List<Officer> findLowPerformingOfficers(double threshold);

    @Query("{ 'consecutiveFraudAttempts': { $gte: 3 } }")
    List<Officer> findRepeatFraudOffenders();
}
