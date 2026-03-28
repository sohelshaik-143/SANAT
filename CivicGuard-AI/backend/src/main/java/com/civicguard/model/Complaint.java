package com.civicguard.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Core complaint entity representing a civic issue reported by a citizen.
 * Stores all lifecycle data from submission through resolution,
 * including AI verification results and escalation history.
 */
@Document(collection = "complaints")
@CompoundIndex(name = "status_dept_idx", def = "{'status': 1, 'assignedDepartment': 1}")
@CompoundIndex(name = "area_status_idx", def = "{'pincode': 1, 'status': 1}")
public class Complaint {

    @Id
    private String id;

    // ─── Complaint Identification ─────────────────────────────
    @Indexed(unique = true)
    private String ticketNumber;        // e.g., "CG-2026-TS-000142"

    private String citizenId;           // Reference to User who submitted
    private String citizenName;
    private String citizenPhone;

    // ─── Issue Details ────────────────────────────────────────
    @Indexed
    private String category;            // IssueCategory enum value
    private String subcategory;
    private String description;
    private String severity;            // LOW, MEDIUM, HIGH, CRITICAL, EMERGENCY

    // ─── Location Data ────────────────────────────────────────
    @GeoSpatialIndexed
    private double[] location;          // [longitude, latitude]
    private String address;
    private String landmark;
    @Indexed
    private String pincode;
    private String district;
    private String state;
    private String ward;                // Municipal ward number

    // ─── Image Evidence ───────────────────────────────────────
    private String originalImagePath;   // Citizen-uploaded photo
    private String resolutionImagePath; // Officer's completion photo
    private ImageVerification imageVerification;  // AI verification results

    // ─── Assignment & Routing ─────────────────────────────────
    @Indexed
    private String assignedDepartment;  // Auto-assigned by AI
    private String assignedOfficerId;
    private String assignedOfficerName;
    private LocalDateTime assignedAt;

    // ─── Status & Lifecycle ───────────────────────────────────
    @Indexed
    private String status;              // SUBMITTED, VERIFIED, ASSIGNED, IN_PROGRESS,
                                        // RESOLUTION_SUBMITTED, RESOLVED, REJECTED, ESCALATED
    private int escalationLevel;        // 0-4 (0=none, 4=state authority)
    private List<EscalationRecord> escalationHistory = new ArrayList<>();
    private List<StatusUpdate> statusHistory = new ArrayList<>();

    // ─── AI Completion Verification ───────────────────────────
    private CompletionVerification completionVerification;
    private boolean fraudDetected;
    private String fraudDetails;

    // ─── Timestamps ───────────────────────────────────────────
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime deadline;     // SLA deadline

    // ─── Nested Classes ───────────────────────────────────────

    /**
     * AI image verification results for the submitted complaint photo.
     */
    public static class ImageVerification {
        private boolean isAuthentic;
        private double authenticityScore;     // 0.0 - 1.0
        private String detectedCategory;
        private double categoryConfidence;
        private boolean isDeepfake;
        private double deepfakeScore;
        private boolean gpsConsistent;
        private boolean timestampValid;
        private String verificationNotes;
        private LocalDateTime verifiedAt;

        // Getters & Setters
        public boolean isAuthentic() { return isAuthentic; }
        public void setAuthentic(boolean authentic) { isAuthentic = authentic; }
        public double getAuthenticityScore() { return authenticityScore; }
        public void setAuthenticityScore(double authenticityScore) { this.authenticityScore = authenticityScore; }
        public String getDetectedCategory() { return detectedCategory; }
        public void setDetectedCategory(String detectedCategory) { this.detectedCategory = detectedCategory; }
        public double getCategoryConfidence() { return categoryConfidence; }
        public void setCategoryConfidence(double categoryConfidence) { this.categoryConfidence = categoryConfidence; }
        public boolean isDeepfake() { return isDeepfake; }
        public void setDeepfake(boolean deepfake) { isDeepfake = deepfake; }
        public double getDeepfakeScore() { return deepfakeScore; }
        public void setDeepfakeScore(double deepfakeScore) { this.deepfakeScore = deepfakeScore; }
        public boolean isGpsConsistent() { return gpsConsistent; }
        public void setGpsConsistent(boolean gpsConsistent) { this.gpsConsistent = gpsConsistent; }
        public boolean isTimestampValid() { return timestampValid; }
        public void setTimestampValid(boolean timestampValid) { this.timestampValid = timestampValid; }
        public String getVerificationNotes() { return verificationNotes; }
        public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }
        public LocalDateTime getVerifiedAt() { return verifiedAt; }
        public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    }

    /**
     * AI verification of the completion/resolution submitted by an officer.
     */
    public static class CompletionVerification {
        private boolean issueResolved;
        private double resolutionConfidence;
        private boolean beforeAfterMatch;     // GPS and location consistency
        private boolean imageTampered;
        private String verificationSummary;
        private LocalDateTime verifiedAt;

        // Getters & Setters
        public boolean isIssueResolved() { return issueResolved; }
        public void setIssueResolved(boolean issueResolved) { this.issueResolved = issueResolved; }
        public double getResolutionConfidence() { return resolutionConfidence; }
        public void setResolutionConfidence(double resolutionConfidence) { this.resolutionConfidence = resolutionConfidence; }
        public boolean isBeforeAfterMatch() { return beforeAfterMatch; }
        public void setBeforeAfterMatch(boolean beforeAfterMatch) { this.beforeAfterMatch = beforeAfterMatch; }
        public boolean isImageTampered() { return imageTampered; }
        public void setImageTampered(boolean imageTampered) { this.imageTampered = imageTampered; }
        public String getVerificationSummary() { return verificationSummary; }
        public void setVerificationSummary(String verificationSummary) { this.verificationSummary = verificationSummary; }
        public LocalDateTime getVerifiedAt() { return verifiedAt; }
        public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    }

    /**
     * Record of each escalation event in the complaint lifecycle.
     */
    public static class EscalationRecord {
        private int level;
        private String fromOfficerId;
        private String toOfficerId;
        private String toDesignation;       // "Block Officer", "District Collector", etc.
        private String reason;
        private LocalDateTime escalatedAt;

        // Getters & Setters
        public int getLevel() { return level; }
        public void setLevel(int level) { this.level = level; }
        public String getFromOfficerId() { return fromOfficerId; }
        public void setFromOfficerId(String fromOfficerId) { this.fromOfficerId = fromOfficerId; }
        public String getToOfficerId() { return toOfficerId; }
        public void setToOfficerId(String toOfficerId) { this.toOfficerId = toOfficerId; }
        public String getToDesignation() { return toDesignation; }
        public void setToDesignation(String toDesignation) { this.toDesignation = toDesignation; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public LocalDateTime getEscalatedAt() { return escalatedAt; }
        public void setEscalatedAt(LocalDateTime escalatedAt) { this.escalatedAt = escalatedAt; }
    }

    /**
     * Status change record for audit trail.
     */
    public static class StatusUpdate {
        private String fromStatus;
        private String toStatus;
        private String updatedBy;
        private String notes;
        private LocalDateTime timestamp;

        // Getters & Setters
        public String getFromStatus() { return fromStatus; }
        public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }
        public String getToStatus() { return toStatus; }
        public void setToStatus(String toStatus) { this.toStatus = toStatus; }
        public String getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    // ─── Main Entity Getters & Setters ────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    public String getCitizenId() { return citizenId; }
    public void setCitizenId(String citizenId) { this.citizenId = citizenId; }
    public String getCitizenName() { return citizenName; }
    public void setCitizenName(String citizenName) { this.citizenName = citizenName; }
    public String getCitizenPhone() { return citizenPhone; }
    public void setCitizenPhone(String citizenPhone) { this.citizenPhone = citizenPhone; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public double[] getLocation() { return location; }
    public void setLocation(double[] location) { this.location = location; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }
    public String getOriginalImagePath() { return originalImagePath; }
    public void setOriginalImagePath(String originalImagePath) { this.originalImagePath = originalImagePath; }
    public String getResolutionImagePath() { return resolutionImagePath; }
    public void setResolutionImagePath(String resolutionImagePath) { this.resolutionImagePath = resolutionImagePath; }
    public ImageVerification getImageVerification() { return imageVerification; }
    public void setImageVerification(ImageVerification imageVerification) { this.imageVerification = imageVerification; }
    public String getAssignedDepartment() { return assignedDepartment; }
    public void setAssignedDepartment(String assignedDepartment) { this.assignedDepartment = assignedDepartment; }
    public String getAssignedOfficerId() { return assignedOfficerId; }
    public void setAssignedOfficerId(String assignedOfficerId) { this.assignedOfficerId = assignedOfficerId; }
    public String getAssignedOfficerName() { return assignedOfficerName; }
    public void setAssignedOfficerName(String assignedOfficerName) { this.assignedOfficerName = assignedOfficerName; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(int escalationLevel) { this.escalationLevel = escalationLevel; }
    public List<EscalationRecord> getEscalationHistory() { return escalationHistory; }
    public void setEscalationHistory(List<EscalationRecord> escalationHistory) { this.escalationHistory = escalationHistory; }
    public List<StatusUpdate> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<StatusUpdate> statusHistory) { this.statusHistory = statusHistory; }
    public CompletionVerification getCompletionVerification() { return completionVerification; }
    public void setCompletionVerification(CompletionVerification completionVerification) { this.completionVerification = completionVerification; }
    public boolean isFraudDetected() { return fraudDetected; }
    public void setFraudDetected(boolean fraudDetected) { this.fraudDetected = fraudDetected; }
    public String getFraudDetails() { return fraudDetails; }
    public void setFraudDetails(String fraudDetails) { this.fraudDetails = fraudDetails; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
}
