package com.civicguard.dto;

import com.civicguard.model.Complaint;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for complaint response — sanitizes internal fields
 * and presents a clean API response to clients.
 */
public class ComplaintResponse {

    private String id;
    private String ticketNumber;
    private String category;
    private String description;
    private String severity;
    private String status;
    private String address;
    private String pincode;
    private String district;
    private String state;
    private double latitude;
    private double longitude;
    private String assignedDepartment;
    private String assignedOfficerName;
    private int escalationLevel;
    private boolean fraudDetected;

    // AI Verification Summary
    private boolean imageVerified;
    private double authenticityScore;
    private String detectedCategory;

    // Timeline
    private LocalDateTime submittedAt;
    private LocalDateTime assignedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime deadline;

    private List<StatusUpdateDTO> statusHistory;

    /**
     * Factory method to create response from Complaint entity.
     */
    public static ComplaintResponse fromEntity(Complaint complaint) {
        ComplaintResponse response = new ComplaintResponse();
        response.setId(complaint.getId());
        response.setTicketNumber(complaint.getTicketNumber());
        response.setCategory(complaint.getCategory());
        response.setDescription(complaint.getDescription());
        response.setSeverity(complaint.getSeverity());
        response.setStatus(complaint.getStatus());
        response.setAddress(complaint.getAddress());
        response.setPincode(complaint.getPincode());
        response.setDistrict(complaint.getDistrict());
        response.setState(complaint.getState());

        if (complaint.getLocation() != null && complaint.getLocation().length == 2) {
            response.setLongitude(complaint.getLocation()[0]);
            response.setLatitude(complaint.getLocation()[1]);
        }

        response.setAssignedDepartment(complaint.getAssignedDepartment());
        response.setAssignedOfficerName(complaint.getAssignedOfficerName());
        response.setEscalationLevel(complaint.getEscalationLevel());
        response.setFraudDetected(complaint.isFraudDetected());
        response.setSubmittedAt(complaint.getCreatedAt());
        response.setAssignedAt(complaint.getAssignedAt());
        response.setResolvedAt(complaint.getResolvedAt());
        response.setDeadline(complaint.getDeadline());

        // AI verification summary
        if (complaint.getImageVerification() != null) {
            response.setImageVerified(complaint.getImageVerification().isAuthentic());
            response.setAuthenticityScore(complaint.getImageVerification().getAuthenticityScore());
            response.setDetectedCategory(complaint.getImageVerification().getDetectedCategory());
        }

        // Map status history
        if (complaint.getStatusHistory() != null) {
            response.setStatusHistory(complaint.getStatusHistory().stream()
                .map(s -> {
                    StatusUpdateDTO dto = new StatusUpdateDTO();
                    dto.setFromStatus(s.getFromStatus());
                    dto.setToStatus(s.getToStatus());
                    dto.setNotes(s.getNotes());
                    dto.setTimestamp(s.getTimestamp());
                    return dto;
                }).toList());
        }

        return response;
    }

    // ─── Inner DTO for Status History ─────────────────────────
    public static class StatusUpdateDTO {
        private String fromStatus;
        private String toStatus;
        private String notes;
        private LocalDateTime timestamp;

        public String getFromStatus() { return fromStatus; }
        public void setFromStatus(String fromStatus) { this.fromStatus = fromStatus; }
        public String getToStatus() { return toStatus; }
        public void setToStatus(String toStatus) { this.toStatus = toStatus; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    // ─── Getters & Setters ────────────────────────────────────

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
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getAssignedDepartment() { return assignedDepartment; }
    public void setAssignedDepartment(String assignedDepartment) { this.assignedDepartment = assignedDepartment; }
    public String getAssignedOfficerName() { return assignedOfficerName; }
    public void setAssignedOfficerName(String assignedOfficerName) { this.assignedOfficerName = assignedOfficerName; }
    public int getEscalationLevel() { return escalationLevel; }
    public void setEscalationLevel(int escalationLevel) { this.escalationLevel = escalationLevel; }
    public boolean isFraudDetected() { return fraudDetected; }
    public void setFraudDetected(boolean fraudDetected) { this.fraudDetected = fraudDetected; }
    public boolean isImageVerified() { return imageVerified; }
    public void setImageVerified(boolean imageVerified) { this.imageVerified = imageVerified; }
    public double getAuthenticityScore() { return authenticityScore; }
    public void setAuthenticityScore(double authenticityScore) { this.authenticityScore = authenticityScore; }
    public String getDetectedCategory() { return detectedCategory; }
    public void setDetectedCategory(String detectedCategory) { this.detectedCategory = detectedCategory; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public List<StatusUpdateDTO> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<StatusUpdateDTO> statusHistory) { this.statusHistory = statusHistory; }
}
