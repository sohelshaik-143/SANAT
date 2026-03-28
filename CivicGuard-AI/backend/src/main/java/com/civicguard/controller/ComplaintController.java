package com.civicguard.controller;

import com.civicguard.dto.ComplaintRequest;
import com.civicguard.dto.ComplaintResponse;
import com.civicguard.service.ComplaintService;
import com.civicguard.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for all complaint operations:
 * - Citizens: Submit complaints, track status, view history
 * - Officers: View assigned complaints, submit resolutions
 * - Admins/Supervisors: Dashboard analytics, heatmaps, reports
 */
@RestController
@RequestMapping("/api")
@CrossOrigin
public class ComplaintController {

    private final ComplaintService complaintService;
    private final ReportService reportService;

    public ComplaintController(ComplaintService complaintService,
                               ReportService reportService) {
        this.complaintService = complaintService;
        this.reportService = reportService;
    }

    // ═══════════════════════════════════════════════════════════
    //  CITIZEN ENDPOINTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Submit a new civic complaint with photo evidence.
     * POST /api/complaints
     * Content-Type: multipart/form-data
     */
    @PostMapping(value = "/complaints", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplaintResponse> submitComplaint(
            Authentication auth,
            @Valid @RequestPart("complaint") ComplaintRequest request,
            @RequestPart("image") MultipartFile image) throws IOException {

        // Validate image
        if (image.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (image.getSize() > 10 * 1024 * 1024) { // 10MB limit
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        }

        String citizenId = auth.getName(); // From JWT
        ComplaintResponse response = complaintService.submitComplaint(citizenId, request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a complaint by ID.
     * GET /api/complaints/{id}
     */
    @GetMapping("/complaints/{id}")
    public ResponseEntity<ComplaintResponse> getComplaint(@PathVariable String id) {
        return ResponseEntity.ok(complaintService.getComplaintById(id));
    }

    /**
     * Track complaint by ticket number (public tracking).
     * GET /api/complaints/track/{ticketNumber}
     */
    @GetMapping("/complaints/track/{ticketNumber}")
    public ResponseEntity<ComplaintResponse> trackComplaint(
            @PathVariable String ticketNumber) {
        return ResponseEntity.ok(complaintService.getByTicketNumber(ticketNumber));
    }

    /**
     * Get current citizen's complaints.
     * GET /api/complaints/my?page=0&size=10
     */
    @GetMapping("/complaints/my")
    public ResponseEntity<Page<ComplaintResponse>> getMyComplaints(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String citizenId = auth.getName();
        Page<ComplaintResponse> complaints = complaintService.getCitizenComplaints(
            citizenId, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(complaints);
    }

    /**
     * Get complaints by area (pincode).
     * GET /api/complaints/area/{pincode}
     */
    @GetMapping("/complaints/area/{pincode}")
    public ResponseEntity<List<ComplaintResponse>> getAreaComplaints(
            @PathVariable String pincode) {
        return ResponseEntity.ok(complaintService.getComplaintsByArea(pincode));
    }

    // ═══════════════════════════════════════════════════════════
    //  OFFICER ENDPOINTS
    // ═══════════════════════════════════════════════════════════

    /**
     * Get complaints assigned to the current officer.
     * GET /api/complaints/assigned?status=ASSIGNED&page=0&size=10
     */
    @GetMapping("/complaints/assigned")
    @PreAuthorize("hasAnyRole('OFFICER', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<Page<ComplaintResponse>> getAssignedComplaints(
            Authentication auth,
            @RequestParam(defaultValue = "ASSIGNED") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String officerId = auth.getName();
        Page<ComplaintResponse> complaints = complaintService.getOfficerComplaints(
            officerId, status, PageRequest.of(page, size, Sort.by("deadline").ascending()));
        return ResponseEntity.ok(complaints);
    }

    /**
     * Submit resolution for a complaint with proof photo.
     * PUT /api/complaints/{id}/resolve
     */
    @PutMapping(value = "/complaints/{id}/resolve",
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OFFICER', 'ADMIN')")
    public ResponseEntity<ComplaintResponse> resolveComplaint(
            @PathVariable String id,
            Authentication auth,
            @RequestPart("image") MultipartFile resolutionImage,
            @RequestPart(value = "notes", required = false) String notes) throws IOException {

        String officerId = auth.getName();
        ComplaintResponse response = complaintService.submitResolution(
            id, officerId, resolutionImage, notes);
        return ResponseEntity.ok(response);
    }

    // ═══════════════════════════════════════════════════════════
    //  DASHBOARD & ANALYTICS (Supervisor/Admin)
    // ═══════════════════════════════════════════════════════════

    /**
     * Dashboard overview statistics.
     * GET /api/dashboard/stats
     */
    @GetMapping("/dashboard/stats")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(reportService.getDashboardStats());
    }

    /**
     * Issue heatmap data for map visualization.
     * GET /api/dashboard/heatmap
     */
    @GetMapping("/dashboard/heatmap")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getHeatmap() {
        return ResponseEntity.ok(reportService.getHeatmapData());
    }

    /**
     * Complaint category distribution.
     * GET /api/dashboard/categories
     */
    @GetMapping("/dashboard/categories")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        return ResponseEntity.ok(reportService.getCategoryDistribution());
    }

    /**
     * Department performance metrics.
     * GET /api/dashboard/departments
     */
    @GetMapping("/dashboard/departments")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getDepartmentPerformance() {
        return ResponseEntity.ok(reportService.getDepartmentPerformance());
    }

    /**
     * Officer performance leaderboard.
     * GET /api/dashboard/officers
     */
    @GetMapping("/dashboard/officers")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getOfficerPerformance() {
        return ResponseEntity.ok(reportService.getOfficerPerformance());
    }

    /**
     * Complaint trend data for line charts.
     * GET /api/dashboard/trend?days=30
     */
    @GetMapping("/dashboard/trend")
    @PreAuthorize("hasAnyRole('SUPERVISOR', 'ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getComplaintTrend(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(reportService.getComplaintTrend(days));
    }
}
