package com.civicguard.service;

import com.civicguard.config.ImageAIConfig;
import com.civicguard.dto.ComplaintRequest;
import com.civicguard.dto.ComplaintResponse;
import com.civicguard.dto.VerificationDTO;
import com.civicguard.model.Complaint;
import com.civicguard.model.Officer;
import com.civicguard.model.User;
import com.civicguard.repository.ComplaintRepository;
import com.civicguard.repository.OfficerRepository;
import com.civicguard.repository.UserRepository;
import com.civicguard.util.ImageUtils;
import com.civicguard.util.NotificationUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Core service managing the complete complaint lifecycle:
 * Submission → AI Verification → Department Routing → Officer Assignment →
 * Resolution → AI Completion Verification → Closure
 *
 * Handles ticket generation, SLA deadlines, status transitions,
 * and coordinates with AI verification and notification services.
 */
@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepo;
    private final UserRepository userRepo;
    private final OfficerRepository officerRepo;
    private final AIImageValidationService aiService;
    private final NotificationUtils notificationUtils;
    private final ImageUtils imageUtils;

    @Value("${complaint.upload.dir:/app/uploads}")
    private String uploadDir;

    // Ticket counter (in production, use MongoDB sequence)
    private static final AtomicLong ticketCounter = new AtomicLong(1000);

    // SLA deadlines by severity (in hours)
    private static final Map<String, Integer> SLA_HOURS = Map.of(
        "EMERGENCY", 4,
        "CRITICAL", 12,
        "HIGH", 24,
        "MEDIUM", 48,
        "LOW", 72
    );

    // State codes for ticket numbers
    private static final Map<String, String> STATE_CODES = Map.ofEntries(
        Map.entry("Andhra Pradesh", "AP"), Map.entry("Telangana", "TS"),
        Map.entry("Karnataka", "KA"), Map.entry("Tamil Nadu", "TN"),
        Map.entry("Kerala", "KL"), Map.entry("Maharashtra", "MH"),
        Map.entry("Gujarat", "GJ"), Map.entry("Rajasthan", "RJ"),
        Map.entry("Uttar Pradesh", "UP"), Map.entry("Madhya Pradesh", "MP"),
        Map.entry("West Bengal", "WB"), Map.entry("Bihar", "BR"),
        Map.entry("Punjab", "PB"), Map.entry("Haryana", "HR"),
        Map.entry("Delhi", "DL"), Map.entry("Odisha", "OD"),
        Map.entry("Assam", "AS"), Map.entry("Jharkhand", "JH"),
        Map.entry("Chhattisgarh", "CG"), Map.entry("Uttarakhand", "UK"),
        Map.entry("Goa", "GA"), Map.entry("Himachal Pradesh", "HP")
    );

    public ComplaintService(ComplaintRepository complaintRepo,
                            UserRepository userRepo,
                            OfficerRepository officerRepo,
                            AIImageValidationService aiService,
                            NotificationUtils notificationUtils,
                            ImageUtils imageUtils) {
        this.complaintRepo = complaintRepo;
        this.userRepo = userRepo;
        this.officerRepo = officerRepo;
        this.aiService = aiService;
        this.notificationUtils = notificationUtils;
        this.imageUtils = imageUtils;
    }

    // ═══════════════════════════════════════════════════════════
    //  COMPLAINT SUBMISSION
    // ═══════════════════════════════════════════════════════════

    /**
     * Submit a new civic complaint with photo evidence.
     * Flow: Save image → AI verify → Generate ticket → Route to department →
     *       Assign officer → Set SLA deadline → Notify
     */
    public ComplaintResponse submitComplaint(String citizenId,
                                             ComplaintRequest request,
                                             MultipartFile image) throws IOException {
        // 1. Validate citizen exists
        User citizen = userRepo.findById(citizenId)
            .orElseThrow(() -> new RuntimeException("Citizen not found: " + citizenId));

        // 2. Save uploaded image
        String imagePath = imageUtils.saveImage(image, uploadDir);

        // 3. AI Image Verification
        VerificationDTO verification = aiService.verifyImage(imagePath,
            request.getLatitude(), request.getLongitude());

        // 4. Build complaint entity
        Complaint complaint = new Complaint();
        complaint.setCitizenId(citizenId);
        complaint.setCitizenName(citizen.getName());
        complaint.setCitizenPhone(citizen.getPhone());
        complaint.setDescription(request.getDescription());
        complaint.setLocation(new double[]{request.getLongitude(), request.getLatitude()});
        complaint.setAddress(request.getAddress());
        complaint.setLandmark(request.getLandmark());
        complaint.setPincode(request.getPincode());
        complaint.setDistrict(request.getDistrict());
        complaint.setState(request.getState());
        complaint.setWard(request.getWard());
        complaint.setOriginalImagePath(imagePath);

        // 5. Apply AI verification results
        Complaint.ImageVerification imgVerification = new Complaint.ImageVerification();
        imgVerification.setAuthentic(verification.isAuthentic());
        imgVerification.setAuthenticityScore(verification.getAuthenticityScore());
        imgVerification.setDetectedCategory(verification.getClassifiedCategory());
        imgVerification.setCategoryConfidence(verification.getCategoryConfidence());
        imgVerification.setDeepfake(verification.isDeepfakeDetected());
        imgVerification.setDeepfakeScore(verification.getDeepfakeConfidence());
        imgVerification.setGpsConsistent(verification.isGpsValid());
        imgVerification.setTimestampValid(verification.isTimestampValid());
        imgVerification.setVerifiedAt(LocalDateTime.now());
        complaint.setImageVerification(imgVerification);

        // 6. Set category (AI-detected or user-specified)
        String category = (request.getCategory() != null && !request.getCategory().isEmpty())
            ? request.getCategory()
            : verification.getClassifiedCategory();
        complaint.setCategory(category);
        complaint.setSubcategory(request.getSubcategory());

        // 7. Determine severity
        String severity = (verification.getSuggestedSeverity() != null)
            ? verification.getSuggestedSeverity() : "MEDIUM";
        complaint.setSeverity(severity);

        // 8. Auto-route to department
        String department = determineDepartment(category, verification.getSuggestedDepartment());
        complaint.setAssignedDepartment(department);

        // 9. Generate ticket number
        String stateCode = STATE_CODES.getOrDefault(request.getState(), "IN");
        String ticketNumber = String.format("CG-%d-%s-%06d",
            LocalDateTime.now().getYear(), stateCode, ticketCounter.incrementAndGet());
        complaint.setTicketNumber(ticketNumber);

        // 10. Set status based on AI verification
        if (!verification.isAuthentic() || verification.isDeepfakeDetected()) {
            complaint.setStatus("REJECTED");
            addStatusUpdate(complaint, null, "REJECTED", "SYSTEM",
                "AI verification failed: image authenticity score too low or deepfake detected");
        } else {
            complaint.setStatus("VERIFIED");
            addStatusUpdate(complaint, null, "VERIFIED", "SYSTEM",
                "AI verification passed with score: " + verification.getAuthenticityScore());

            // 11. Auto-assign officer
            assignOfficer(complaint);

            // 12. Set SLA deadline
            int slaHours = SLA_HOURS.getOrDefault(severity, 48);
            complaint.setDeadline(LocalDateTime.now().plusHours(slaHours));
        }

        // 13. Save complaint
        Complaint saved = complaintRepo.save(complaint);

        // 14. Update citizen stats
        citizen.setTotalComplaints(citizen.getTotalComplaints() + 1);
        userRepo.save(citizen);

        // 15. Send notifications
        notificationUtils.sendComplaintConfirmation(citizen.getPhone(),
            citizen.getEmail(), ticketNumber, category);

        return ComplaintResponse.fromEntity(saved);
    }

    // ═══════════════════════════════════════════════════════════
    //  COMPLAINT RESOLUTION
    // ═══════════════════════════════════════════════════════════

    /**
     * Officer submits resolution with proof photo.
     * AI verifies the completion before marking as resolved.
     */
    public ComplaintResponse submitResolution(String complaintId,
                                              String officerId,
                                              MultipartFile resolutionImage,
                                              String notes) throws IOException {
        Complaint complaint = complaintRepo.findById(complaintId)
            .orElseThrow(() -> new RuntimeException("Complaint not found: " + complaintId));

        // Verify officer is assigned to this complaint
        if (!officerId.equals(complaint.getAssignedOfficerId())) {
            throw new RuntimeException("Officer not authorized for this complaint");
        }

        // Save resolution image
        String resImagePath = imageUtils.saveImage(resolutionImage, uploadDir + "/resolutions");
        complaint.setResolutionImagePath(resImagePath);

        // AI Completion Verification — compare before & after
        VerificationDTO completionResult = aiService.verifyCompletion(
            complaint.getOriginalImagePath(), resImagePath,
            complaint.getLocation()[1], complaint.getLocation()[0]);

        // Build completion verification record
        Complaint.CompletionVerification cv = new Complaint.CompletionVerification();
        cv.setIssueResolved(completionResult.isCompletionVerified());
        cv.setResolutionConfidence(completionResult.getCompletionConfidence());
        cv.setBeforeAfterMatch(completionResult.isBeforeAfterConsistent());
        cv.setImageTampered(completionResult.isFraudIndicators());
        cv.setVerificationSummary(completionResult.getCompletionNotes());
        cv.setVerifiedAt(LocalDateTime.now());
        complaint.setCompletionVerification(cv);

        // Check for fraud in resolution
        if (completionResult.isFraudIndicators()) {
            complaint.setFraudDetected(true);
            complaint.setFraudDetails(completionResult.getFraudDetails());
            complaint.setStatus("FRAUD_DETECTED");
            addStatusUpdate(complaint, "RESOLUTION_SUBMITTED", "FRAUD_DETECTED",
                officerId, "AI detected fraudulent resolution: " + completionResult.getFraudType());

            // Flag the officer
            flagOfficerForFraud(officerId, complaintId, completionResult.getFraudType());
        } else if (completionResult.isCompletionVerified()) {
            complaint.setStatus("RESOLVED");
            complaint.setResolvedAt(LocalDateTime.now());
            addStatusUpdate(complaint, "RESOLUTION_SUBMITTED", "RESOLVED",
                officerId, "AI verified resolution. Confidence: " + completionResult.getCompletionConfidence());

            // Update citizen stats
            userRepo.findById(complaint.getCitizenId()).ifPresent(citizen -> {
                citizen.setResolvedComplaints(citizen.getResolvedComplaints() + 1);
                userRepo.save(citizen);
            });

            // Update officer stats
            updateOfficerResolutionStats(officerId);
        } else {
            complaint.setStatus("RESOLUTION_SUBMITTED");
            addStatusUpdate(complaint, "IN_PROGRESS", "RESOLUTION_SUBMITTED",
                officerId, "Resolution submitted, pending manual review. Notes: " + notes);
        }

        Complaint saved = complaintRepo.save(complaint);

        // Notify citizen about resolution status
        notificationUtils.sendResolutionUpdate(complaint.getCitizenPhone(),
            complaint.getTicketNumber(), complaint.getStatus());

        return ComplaintResponse.fromEntity(saved);
    }

    // ═══════════════════════════════════════════════════════════
    //  QUERY METHODS
    // ═══════════════════════════════════════════════════════════

    public ComplaintResponse getComplaintById(String id) {
        Complaint complaint = complaintRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Complaint not found: " + id));
        return ComplaintResponse.fromEntity(complaint);
    }

    public ComplaintResponse getByTicketNumber(String ticketNumber) {
        Complaint complaint = complaintRepo.findByTicketNumber(ticketNumber)
            .orElseThrow(() -> new RuntimeException("Ticket not found: " + ticketNumber));
        return ComplaintResponse.fromEntity(complaint);
    }

    public Page<ComplaintResponse> getCitizenComplaints(String citizenId, Pageable pageable) {
        return complaintRepo.findByCitizenId(citizenId, pageable)
            .map(ComplaintResponse::fromEntity);
    }

    public Page<ComplaintResponse> getOfficerComplaints(String officerId,
                                                         String status,
                                                         Pageable pageable) {
        return complaintRepo.findByAssignedOfficerIdAndStatus(officerId, status, pageable)
            .map(ComplaintResponse::fromEntity);
    }

    public List<ComplaintResponse> getComplaintsByArea(String pincode) {
        return complaintRepo.findByPincode(pincode).stream()
            .map(ComplaintResponse::fromEntity)
            .toList();
    }

    // ═══════════════════════════════════════════════════════════
    //  INTERNAL HELPERS
    // ═══════════════════════════════════════════════════════════

    /**
     * Auto-assign complaint to the most suitable officer based on
     * department, jurisdiction (pincode/district), and current workload.
     */
    private void assignOfficer(Complaint complaint) {
        String department = complaint.getAssignedDepartment();
        String pincode = complaint.getPincode();
        String district = complaint.getDistrict();

        // Try pincode-level match first
        List<Officer> candidates = officerRepo.findByDepartmentAndPincode(department, pincode);

        // Fallback to district-level
        if (candidates.isEmpty() && district != null) {
            candidates = officerRepo.findByDepartmentAndDistrictAndActiveTrue(department, district);
        }

        // Fallback to any officer in department
        if (candidates.isEmpty()) {
            candidates = officerRepo.findByDepartmentAndActiveTrue(department);
        }

        if (!candidates.isEmpty()) {
            // Pick officer with lowest current workload
            Officer assigned = candidates.stream()
                .min(Comparator.comparingInt(Officer::getTotalAssigned))
                .orElse(candidates.get(0));

            complaint.setAssignedOfficerId(assigned.getId());
            complaint.setAssignedOfficerName(assigned.getName());
            complaint.setAssignedAt(LocalDateTime.now());
            complaint.setStatus("ASSIGNED");
            addStatusUpdate(complaint, "VERIFIED", "ASSIGNED", "SYSTEM",
                "Auto-assigned to " + assigned.getName() + " (" + assigned.getDesignation() + ")");

            // Increment officer's assigned count
            assigned.setTotalAssigned(assigned.getTotalAssigned() + 1);
            officerRepo.save(assigned);

            // Notify the officer
            notificationUtils.sendOfficerAssignment(assigned.getPhone(),
                assigned.getEmail(), complaint.getTicketNumber(), complaint.getCategory());
        }
    }

    /**
     * Determine the correct government department based on issue category.
     */
    private String determineDepartment(String category, String aiSuggested) {
        if (aiSuggested != null && !aiSuggested.isEmpty()) {
            return aiSuggested;
        }
        // Fallback mapping
        return switch (category != null ? category.toUpperCase() : "") {
            case "POTHOLE", "ROAD_SIGN", "BRIDGE_DAMAGE" -> "PWD";
            case "GARBAGE", "SEWAGE", "CONSTRUCTION_DEBRIS" -> "Municipal Sanitation";
            case "DRAINAGE", "WATER_LEAK" -> "Water Board";
            case "STREETLIGHT" -> "Municipal Electricity";
            case "ENCROACHMENT", "TREE_FALL" -> "Municipal Corporation";
            case "NOISE_POLLUTION", "AIR_POLLUTION" -> "Pollution Control Board";
            case "STRAY_ANIMALS" -> "Animal Husbandry";
            default -> "Municipal Corporation";
        };
    }

    /**
     * Add a status transition record to the complaint's audit trail.
     */
    private void addStatusUpdate(Complaint complaint, String from, String to,
                                  String updatedBy, String notes) {
        Complaint.StatusUpdate update = new Complaint.StatusUpdate();
        update.setFromStatus(from);
        update.setToStatus(to);
        update.setUpdatedBy(updatedBy);
        update.setNotes(notes);
        update.setTimestamp(LocalDateTime.now());
        complaint.getStatusHistory().add(update);
    }

    /**
     * Flag an officer for submitting fraudulent resolution evidence.
     */
    private void flagOfficerForFraud(String officerId, String complaintId, String fraudType) {
        officerRepo.findById(officerId).ifPresent(officer -> {
            officer.setTotalFraudFlags(officer.getTotalFraudFlags() + 1);
            officer.setConsecutiveFraudAttempts(officer.getConsecutiveFraudAttempts() + 1);

            // Auto-flag after 3 consecutive fraud attempts
            if (officer.getConsecutiveFraudAttempts() >= 3) {
                officer.setFlaggedForReview(true);
                officer.setFlagReason("RED FLAG: " + officer.getConsecutiveFraudAttempts() +
                    " consecutive fraud attempts. Latest: " + fraudType +
                    " on complaint " + complaintId);
            }

            officerRepo.save(officer);
        });
    }

    /**
     * Update officer statistics after successful resolution.
     */
    private void updateOfficerResolutionStats(String officerId) {
        officerRepo.findById(officerId).ifPresent(officer -> {
            officer.setTotalResolved(officer.getTotalResolved() + 1);
            officer.setConsecutiveFraudAttempts(0); // Reset on good resolution

            // Recalculate performance score
            if (officer.getTotalAssigned() > 0) {
                double score = ((double) officer.getTotalResolved() / officer.getTotalAssigned()) * 100;
                score -= (officer.getTotalFraudFlags() * 10); // Penalty for fraud
                score -= (officer.getTotalEscalated() * 5);   // Penalty for escalations
                officer.setPerformanceScore(Math.max(0, Math.min(100, score)));
            }

            officerRepo.save(officer);
        });
    }
}
