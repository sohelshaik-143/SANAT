package com.civicguard.ui.models;

import java.time.LocalDateTime;

/**
 * Client-side complaint model for the JavaFX desktop application.
 * Mirrors the ComplaintResponse DTO from the backend API.
 */
public class ComplaintModel {

    private String id;
    private String ticketNumber;
    private String category;
    private String description;
    private String severity;
    private String status;
    private String address;
    private String pincode;
    private String district;
    private String assignedDepartment;
    private String assignedOfficerName;
    private int escalationLevel;
    private boolean fraudDetected;
    private double authenticityScore;
    private LocalDateTime submittedAt;
    private LocalDateTime deadline;
    private LocalDateTime resolvedAt;

    // JavaFX TableView requires property accessors
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTicketNumber() { return ticketNumber; }
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getAssignedDepartment() { return assignedDepartment; }
    public void setAssignedDepartment(String assignedDepartment) { this.assignedDepartment = assignedDepartment; }
    public String getAssignedOfficerName() { return assignedOfficerName; }
    public void setAssignedOfficerName(String assignedOfficerName) { this.assignedOfficerName = assignedOfficerName; }
    public int getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(int escalationLevel) { this.escalationLevel = escalationLevel; }
    public boolean isFraudDetected() { return fraudDetected; }
    public void setFraudDetected(boolean fraudDetected) { this.fraudDetected = fraudDetected; }
    public double getAuthenticityScore() { return authenticityScore; }
    public void setAuthenticityScore(double authenticityScore) { this.authenticityScore = authenticityScore; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    /**
     * Check if this complaint is overdue based on its deadline.
     */
    public boolean isOverdue() {
        return deadline != null && LocalDateTime.now().isAfter(deadline)
            && !"RESOLVED".equals(status) && !"REJECTED".equals(status);
    }

    /**
     * Get a severity display color for the UI.
     */
    public String getSeverityColor() {
        return switch (severity != null ? severity : "") {
            case "EMERGENCY" -> "#ef4444";
            case "CRITICAL" -> "#f97316";
            case "HIGH" -> "#eab308";
            case "MEDIUM" -> "#3b82f6";
            case "LOW" -> "#22c55e";
            default -> "#94a3b8";
        };
    }

    @Override
    public String toString() {
        return ticketNumber + " | " + category + " | " + status;
    }
}
