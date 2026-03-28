package com.civicguard.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Officer entity representing government officials at various levels.
 * Officers are assigned complaints and responsible for resolution.
 * The designation determines their position in the escalation chain.
 */
@Document(collection = "officers")
public class Officer {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String phone;

    private String password;                 // BCrypt hashed

    private String role;                     // OFFICER, SUPERVISOR, ADMIN

    // ─── Government Designation ───────────────────────────────
    @Indexed
    private String designation;              // Field Officer, Block Officer,
                                             // District Collector, Commissioner, etc.
    private int escalationTier;              // 1=Field, 2=Block, 3=District, 4=State
    @Indexed
    private String department;               // PWD, Municipal, Water Board, etc.
    private String employeeId;               // Government employee ID

    // ─── Jurisdiction ─────────────────────────────────────────
    @Indexed
    private String state;
    @Indexed
    private String district;
    private String city;
    private List<String> assignedWards = new ArrayList<>();
    private List<String> assignedPincodes = new ArrayList<>();

    // ─── Performance Metrics ──────────────────────────────────
    private int totalAssigned;
    private int totalResolved;
    private int totalEscalated;              // Escalated FROM this officer
    private int totalFraudFlags;             // Fake resolution attempts
    private double avgResolutionHours;
    private double performanceScore;         // 0-100

    // ─── Fraud Tracking ───────────────────────────────────────
    private int consecutiveFraudAttempts;
    private boolean flaggedForReview;
    private String flagReason;

    private boolean active;

    @CreatedDate
    private LocalDateTime createdAt;

    // ─── Getters & Setters ────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public int getEscalationTier() { return escalationTier; }
    public void setEscalationTier(int escalationTier) { this.escalationTier = escalationTier; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public List<String> getAssignedWards() { return assignedWards; }
    public void setAssignedWards(List<String> assignedWards) { this.assignedWards = assignedWards; }
    public List<String> getAssignedPincodes() { return assignedPincodes; }
    public void setAssignedPincodes(List<String> assignedPincodes) { this.assignedPincodes = assignedPincodes; }
    public int getTotalAssigned() { return totalAssigned; }
    public void setTotalAssigned(int totalAssigned) { this.totalAssigned = totalAssigned; }
    public int getTotalResolved() { return totalResolved; }
    public void setTotalResolved(int totalResolved) { this.totalResolved = totalResolved; }
    public int getTotalEscalated() { return totalEscalated; }
    public void setTotalEscalated(int totalEscalated) { this.totalEscalated = totalEscalated; }
    public int getTotalFraudFlags() { return totalFraudFlags; }
    public void setTotalFraudFlags(int totalFraudFlags) { this.totalFraudFlags = totalFraudFlags; }
    public double getAvgResolutionHours() { return avgResolutionHours; }
    public void setAvgResolutionHours(double avgResolutionHours) { this.avgResolutionHours = avgResolutionHours; }
    public double getPerformanceScore() { return performanceScore; }
    public void setPerformanceScore(double performanceScore) { this.performanceScore = performanceScore; }
    public int getConsecutiveFraudAttempts() { return consecutiveFraudAttempts; }
    public void setConsecutiveFraudAttempts(int consecutiveFraudAttempts) { this.consecutiveFraudAttempts = consecutiveFraudAttempts; }
    public boolean isFlaggedForReview() { return flaggedForReview; }
    public void setFlaggedForReview(boolean flaggedForReview) { this.flaggedForReview = flaggedForReview; }
    public String getFlagReason() { return flagReason; }
    public void setFlagReason(String flagReason) { this.flagReason = flagReason; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
