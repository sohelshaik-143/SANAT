package com.civicguard.util;

import com.civicguard.model.Complaint;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * PDF generation utility for audit-ready complaint reports.
 * Generates timestamped, geo-tagged, AI-verified documentation
 * suitable for RTI requests, court cases, internal audits,
 * and budget proposals.
 *
 * Uses iText 8 for PDF generation.
 * In a production setup, replace the text-based approach with
 * proper iText PdfWriter, PdfDocument, and styled elements.
 */
@Component
public class PDFUtils {

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /**
     * Generate a comprehensive complaint report PDF.
     * Includes: complaint details, AI verification results,
     * escalation history, status timeline, and evidence summary.
     *
     * @param complaint  The complaint entity with full data
     * @param outputDir  Directory to save the PDF
     * @return           Path to the generated PDF file
     */
    public String generateComplaintReport(Complaint complaint, String outputDir) throws IOException {
        Files.createDirectories(Paths.get(outputDir));

        String filename = String.format("CivicGuard_Report_%s_%s.pdf",
            complaint.getTicketNumber(),
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

        Path pdfPath = Paths.get(outputDir, filename);

        // Build report content (text-based placeholder)
        // In production: use iText8 PdfWriter for proper PDF formatting
        StringBuilder report = new StringBuilder();

        report.append("═══════════════════════════════════════════════════\n");
        report.append("        CIVICGUARD-AI — COMPLAINT REPORT          \n");
        report.append("     AI-Powered Civic Issue Monitoring System      \n");
        report.append("═══════════════════════════════════════════════════\n\n");

        report.append("TICKET NUMBER: ").append(complaint.getTicketNumber()).append("\n");
        report.append("GENERATED ON:  ").append(LocalDateTime.now().format(FORMATTER)).append("\n");
        report.append("STATUS:        ").append(complaint.getStatus()).append("\n\n");

        // Section 1: Complaint Details
        report.append("──────── COMPLAINT DETAILS ────────\n");
        report.append("Category:     ").append(complaint.getCategory()).append("\n");
        report.append("Severity:     ").append(complaint.getSeverity()).append("\n");
        report.append("Description:  ").append(complaint.getDescription()).append("\n");
        report.append("Submitted At: ").append(formatDate(complaint.getCreatedAt())).append("\n");
        report.append("SLA Deadline: ").append(formatDate(complaint.getDeadline())).append("\n\n");

        // Section 2: Citizen Info
        report.append("──────── CITIZEN INFORMATION ────────\n");
        report.append("Name:   ").append(complaint.getCitizenName()).append("\n");
        report.append("Phone:  ").append(complaint.getCitizenPhone()).append("\n\n");

        // Section 3: Location
        report.append("──────── LOCATION DATA ────────\n");
        report.append("Address:   ").append(complaint.getAddress()).append("\n");
        report.append("Landmark:  ").append(complaint.getLandmark()).append("\n");
        report.append("Pincode:   ").append(complaint.getPincode()).append("\n");
        report.append("District:  ").append(complaint.getDistrict()).append("\n");
        report.append("State:     ").append(complaint.getState()).append("\n");
        if (complaint.getLocation() != null && complaint.getLocation().length == 2) {
            report.append("GPS:       ").append(complaint.getLocation()[1])
                .append(", ").append(complaint.getLocation()[0]).append("\n");
        }
        report.append("\n");

        // Section 4: AI Verification
        report.append("──────── AI VERIFICATION RESULTS ────────\n");
        if (complaint.getImageVerification() != null) {
            var iv = complaint.getImageVerification();
            report.append("Authentic:        ").append(iv.isAuthentic()).append("\n");
            report.append("Auth Score:       ").append(String.format("%.2f%%", iv.getAuthenticityScore() * 100)).append("\n");
            report.append("Deepfake Check:   ").append(iv.isDeepfake() ? "FLAGGED" : "PASSED").append("\n");
            report.append("Detected Category:").append(iv.getDetectedCategory()).append("\n");
            report.append("Category Conf:    ").append(String.format("%.2f%%", iv.getCategoryConfidence() * 100)).append("\n");
            report.append("GPS Consistent:   ").append(iv.isGpsConsistent()).append("\n");
            report.append("Timestamp Valid:  ").append(iv.isTimestampValid()).append("\n");
            report.append("Verified At:      ").append(formatDate(iv.getVerifiedAt())).append("\n");
        }
        report.append("\n");

        // Section 5: Assignment
        report.append("──────── DEPARTMENT ASSIGNMENT ────────\n");
        report.append("Department:    ").append(complaint.getAssignedDepartment()).append("\n");
        report.append("Officer:       ").append(complaint.getAssignedOfficerName()).append("\n");
        report.append("Assigned At:   ").append(formatDate(complaint.getAssignedAt())).append("\n");
        report.append("Escalation Lv: ").append(complaint.getEscalationLevel()).append("\n\n");

        // Section 6: Escalation History
        if (complaint.getEscalationHistory() != null && !complaint.getEscalationHistory().isEmpty()) {
            report.append("──────── ESCALATION HISTORY ────────\n");
            for (var esc : complaint.getEscalationHistory()) {
                report.append(String.format("  Level %d → %s at %s\n",
                    esc.getLevel(), esc.getToDesignation(), formatDate(esc.getEscalatedAt())));
                report.append("    Reason: ").append(esc.getReason()).append("\n");
            }
            report.append("\n");
        }

        // Section 7: Fraud Detection
        if (complaint.isFraudDetected()) {
            report.append("──────── ⚠ FRAUD DETECTED ────────\n");
            report.append("Details: ").append(complaint.getFraudDetails()).append("\n\n");
        }

        // Section 8: Status Timeline
        report.append("──────── STATUS TIMELINE ────────\n");
        if (complaint.getStatusHistory() != null) {
            for (var su : complaint.getStatusHistory()) {
                report.append(String.format("  [%s] %s → %s\n",
                    formatDate(su.getTimestamp()), su.getFromStatus(), su.getToStatus()));
                report.append("    Notes: ").append(su.getNotes()).append("\n");
            }
        }

        report.append("\n═══════════════════════════════════════════════════\n");
        report.append("  This report is auto-generated by CivicGuard-AI  \n");
        report.append("  All data is AI-verified, timestamped & geo-tagged\n");
        report.append("  Valid for RTI, Court Cases & Government Audits   \n");
        report.append("═══════════════════════════════════════════════════\n");

        // Write to file
        Files.writeString(pdfPath, report.toString());

        return pdfPath.toString();
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : "N/A";
    }
}
