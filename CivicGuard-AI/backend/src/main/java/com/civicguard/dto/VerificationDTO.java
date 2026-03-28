package com.civicguard.dto;

import java.time.LocalDateTime;

/**
 * DTO for AI verification results — used for both
 * initial image verification and completion verification.
 */
public class VerificationDTO {

    // ─── Image Authenticity ───────────────────────────────────
    private boolean authentic;
    private double authenticityScore;
    private boolean deepfakeDetected;
    private double deepfakeConfidence;

    // ─── Category Classification ──────────────────────────────
    private String classifiedCategory;
    private double categoryConfidence;
    private String suggestedDepartment;
    private String suggestedSeverity;

    // ─── GPS & Timestamp Validation ───────────────────────────
    private boolean gpsValid;
    private boolean timestampValid;
    private boolean locationMatchesImage;

    // ─── Fraud Detection ──────────────────────────────────────
    private boolean fraudIndicators;
    private String fraudType;            // RECYCLED_IMAGE, EDITED_PHOTO, 
                                         // FAKE_DOCUMENT, GPS_MISMATCH, etc.
    private String fraudDetails;
    private double fraudRiskScore;       // 0.0 - 1.0

    // ─── Completion Verification ──────────────────────────────
    private boolean completionVerified;
    private double completionConfidence;
    private boolean beforeAfterConsistent;
    private String completionNotes;

    // ─── Metadata ─────────────────────────────────────────────
    private String modelVersion;
    private LocalDateTime processedAt;
    private long processingTimeMs;

    // ─── Getters & Setters ────────────────────────────────────

    public boolean isAuthentic() { return authentic; }
    public void setAuthentic(boolean authentic) { this.authentic = authentic; }
    public double getAuthenticityScore() { return authenticityScore; }
    public void setAuthenticityScore(double authenticityScore) { this.authenticityScore = authenticityScore; }
    public boolean isDeepfakeDetected() { return deepfakeDetected; }
    public void setDeepfakeDetected(boolean deepfakeDetected) { this.deepfakeDetected = deepfakeDetected; }
    public double getDeepfakeConfidence() { return deepfakeConfidence; }
    public void setDeepfakeConfidence(double deepfakeConfidence) { this.deepfakeConfidence = deepfakeConfidence; }
    public String getClassifiedCategory() { return classifiedCategory; }
    public void setClassifiedCategory(String classifiedCategory) { this.classifiedCategory = classifiedCategory; }
    public double getCategoryConfidence() { return categoryConfidence; }
    public void setCategoryConfidence(double categoryConfidence) { this.categoryConfidence = categoryConfidence; }
    public String getSuggestedDepartment() { return suggestedDepartment; }
    public void setSuggestedDepartment(String suggestedDepartment) { this.suggestedDepartment = suggestedDepartment; }
    public String getSuggestedSeverity() { return suggestedSeverity; }
    public void setSuggestedSeverity(String suggestedSeverity) { this.suggestedSeverity = suggestedSeverity; }
    public boolean isGpsValid() { return gpsValid; }
    public void setGpsValid(boolean gpsValid) { this.gpsValid = gpsValid; }
    public boolean isTimestampValid() { return timestampValid; }
    public void setTimestampValid(boolean timestampValid) { this.timestampValid = timestampValid; }
    public boolean isLocationMatchesImage() { return locationMatchesImage; }
    public void setLocationMatchesImage(boolean locationMatchesImage) { this.locationMatchesImage = locationMatchesImage; }
    public boolean isFraudIndicators() { return fraudIndicators; }
    public void setFraudIndicators(boolean fraudIndicators) { this.fraudIndicators = fraudIndicators; }
    public String getFraudType() { return fraudType; }
    public void setFraudType(String fraudType) { this.fraudType = fraudType; }
    public String getFraudDetails() { return fraudDetails; }
    public void setFraudDetails(String fraudDetails) { this.fraudDetails = fraudDetails; }
    public double getFraudRiskScore() { return fraudRiskScore; }
    public void setFraudRiskScore(double fraudRiskScore) { this.fraudRiskScore = fraudRiskScore; }
    public boolean isCompletionVerified() { return completionVerified; }
    public void setCompletionVerified(boolean completionVerified) { this.completionVerified = completionVerified; }
    public double getCompletionConfidence() { return completionConfidence; }
    public void setCompletionConfidence(double completionConfidence) { this.completionConfidence = completionConfidence; }
    public boolean isBeforeAfterConsistent() { return beforeAfterConsistent; }
    public void setBeforeAfterConsistent(boolean beforeAfterConsistent) { this.beforeAfterConsistent = beforeAfterConsistent; }
    public String getCompletionNotes() { return completionNotes; }
    public void setCompletionNotes(String completionNotes) { this.completionNotes = completionNotes; }
    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
}
