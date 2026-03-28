package com.civicguard.service;

import com.civicguard.model.Complaint;
import com.civicguard.model.Officer;
import com.civicguard.repository.ComplaintRepository;
import com.civicguard.repository.OfficerRepository;
import com.civicguard.util.NotificationUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Automatic Escalation Engine — the core accountability mechanism.
 * 
 * Runs on a scheduled basis to check for overdue complaints and
 * escalates them through the government hierarchy:
 * 
 *   Level 0 → Level 1: Field Officer (initial assignment)
 *   Level 1 → Level 2: Block Officer / Supervisor
 *   Level 2 → Level 3: District Collector
 *   Level 3 → Level 4: State Commissioner / Secretary
 * 
 * Each escalation:
 * - Notifies the new authority via SMS + Email
 * - Records the escalation in the complaint's audit trail
 * - Updates the previous officer's performance metrics
 * - Sends a warning to the non-responsive officer
 *
 * Escalation Rules (loaded from escalation-rules.json):
 * - EMERGENCY: Escalate after 4 hours
 * - CRITICAL:  Escalate after 12 hours
 * - HIGH:      Escalate after 24 hours
 * - MEDIUM:    Escalate after 48 hours
 * - LOW:       Escalate after 72 hours
 */
@Service
public class AutoEscalationService {

    private final ComplaintRepository complaintRepo;
    private final OfficerRepository officerRepo;
    private final NotificationUtils notificationUtils;

    // Designation labels for each escalation tier
    private static final String[] TIER_DESIGNATIONS = {
        "Unassigned",
        "Field Officer",
        "Block Officer / Supervisor",
        "District Collector",
        "State Commissioner"
    };

    public AutoEscalationService(ComplaintRepository complaintRepo,
                                  OfficerRepository officerRepo,
                                  NotificationUtils notificationUtils) {
        this.complaintRepo = complaintRepo;
        this.officerRepo = officerRepo;
        this.notificationUtils = notificationUtils;
    }

    // ═══════════════════════════════════════════════════════════
    //  SCHEDULED ESCALATION CHECK (runs every 15 minutes)
    // ═══════════════════════════════════════════════════════════

    /**
     * Main scheduled job: find all overdue complaints and escalate them.
     * Cron: Every 15 minutes, 24/7.
     */
    @Scheduled(fixedRate = 900000) // 15 minutes in milliseconds
    public void checkAndEscalateOverdueComplaints() {
        LocalDateTime now = LocalDateTime.now();
        List<Complaint> overdueComplaints = complaintRepo.findOverdueComplaints(now);

        System.out.println("[ESCALATION ENGINE] Checking overdue complaints at " + now +
            ". Found: " + overdueComplaints.size());

        for (Complaint complaint : overdueComplaints) {
            try {
                escalateComplaint(complaint);
            } catch (Exception e) {
                System.err.println("[ESCALATION ENGINE] Error escalating " +
                    complaint.getTicketNumber() + ": " + e.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  ESCALATION LOGIC
    // ═══════════════════════════════════════════════════════════

    /**
     * Escalate a single complaint to the next tier.
     */
    public void escalateComplaint(Complaint complaint) {
        int currentLevel = complaint.getEscalationLevel();
        int nextLevel = currentLevel + 1;

        if (nextLevel > 4) {
            System.out.println("[ESCALATION] Maximum escalation reached for " +
                complaint.getTicketNumber());
            return;
        }

        String previousOfficerId = complaint.getAssignedOfficerId();

        // Find the next-tier officer
        Officer nextOfficer = findEscalationTarget(
            complaint.getAssignedDepartment(),
            complaint.getDistrict(),
            nextLevel
        );

        // Record the escalation
        Complaint.EscalationRecord record = new Complaint.EscalationRecord();
        record.setLevel(nextLevel);
        record.setFromOfficerId(previousOfficerId);
        record.setToOfficerId(nextOfficer != null ? nextOfficer.getId() : null);
        record.setToDesignation(TIER_DESIGNATIONS[nextLevel]);
        record.setReason("SLA deadline exceeded. Auto-escalated from Level " +
            currentLevel + " to Level " + nextLevel);
        record.setEscalatedAt(LocalDateTime.now());
        complaint.getEscalationHistory().add(record);

        // Update complaint
        complaint.setEscalationLevel(nextLevel);
        complaint.setStatus("ESCALATED");

        // Add status history
        Complaint.StatusUpdate statusUpdate = new Complaint.StatusUpdate();
        statusUpdate.setFromStatus(complaint.getStatus());
        statusUpdate.setToStatus("ESCALATED");
        statusUpdate.setUpdatedBy("AUTO_ESCALATION_ENGINE");
        statusUpdate.setNotes("Escalated to " + TIER_DESIGNATIONS[nextLevel] +
            (nextOfficer != null ? " (" + nextOfficer.getName() + ")" : ""));
        statusUpdate.setTimestamp(LocalDateTime.now());
        complaint.getStatusHistory().add(statusUpdate);

        // Re-assign to new officer
        if (nextOfficer != null) {
            complaint.setAssignedOfficerId(nextOfficer.getId());
            complaint.setAssignedOfficerName(nextOfficer.getName());

            // Set new deadline based on escalation level
            int newDeadlineHours = getEscalationDeadlineHours(nextLevel, complaint.getSeverity());
            complaint.setDeadline(LocalDateTime.now().plusHours(newDeadlineHours));
        }

        complaintRepo.save(complaint);

        // ─── Send Notifications ──────────────────────────────

        // Notify new authority
        if (nextOfficer != null) {
            notificationUtils.sendEscalationAlert(
                nextOfficer.getPhone(),
                nextOfficer.getEmail(),
                complaint.getTicketNumber(),
                nextLevel,
                complaint.getCategory(),
                complaint.getSeverity()
            );
        }

        // Warn the previous officer
        if (previousOfficerId != null) {
            officerRepo.findById(previousOfficerId).ifPresent(prevOfficer -> {
                prevOfficer.setTotalEscalated(prevOfficer.getTotalEscalated() + 1);

                // Recalculate performance score
                if (prevOfficer.getTotalAssigned() > 0) {
                    double score = ((double) prevOfficer.getTotalResolved() /
                        prevOfficer.getTotalAssigned()) * 100;
                    score -= (prevOfficer.getTotalEscalated() * 5);
                    score -= (prevOfficer.getTotalFraudFlags() * 10);
                    prevOfficer.setPerformanceScore(Math.max(0, Math.min(100, score)));
                }

                officerRepo.save(prevOfficer);

                notificationUtils.sendEscalationWarning(
                    prevOfficer.getPhone(),
                    prevOfficer.getEmail(),
                    complaint.getTicketNumber(),
                    nextLevel
                );
            });
        }

        // Notify citizen
        notificationUtils.sendEscalationNotification(
            complaint.getCitizenPhone(),
            complaint.getTicketNumber(),
            nextLevel,
            TIER_DESIGNATIONS[nextLevel]
        );

        System.out.println("[ESCALATION] " + complaint.getTicketNumber() +
            " escalated to Level " + nextLevel + " (" + TIER_DESIGNATIONS[nextLevel] + ")");
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPER METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Find the best officer at the target escalation tier.
     */
    private Officer findEscalationTarget(String department, String district, int tier) {
        List<Officer> candidates = officerRepo.findEscalationTarget(department, district, tier);

        if (candidates.isEmpty()) {
            // Try broader search (department-level only)
            candidates = officerRepo.findByEscalationTierAndDepartmentAndActiveTrue(tier, department);
        }

        if (candidates.isEmpty()) {
            return null;
        }

        // Pick the officer with best performance and lowest current workload
        return candidates.stream()
            .sorted(Comparator
                .comparingDouble(Officer::getPerformanceScore).reversed()
                .thenComparingInt(Officer::getTotalAssigned))
            .findFirst()
            .orElse(candidates.get(0));
    }

    /**
     * Get new deadline hours based on escalation level and severity.
     * Higher escalation levels get tighter deadlines.
     */
    private int getEscalationDeadlineHours(int level, String severity) {
        int baseHours = switch (severity != null ? severity : "MEDIUM") {
            case "EMERGENCY" -> 2;
            case "CRITICAL" -> 6;
            case "HIGH" -> 12;
            case "MEDIUM" -> 24;
            case "LOW" -> 48;
            default -> 24;
        };

        // Each escalation level reduces deadline by 50%
        return Math.max(1, baseHours / level);
    }

    // ═══════════════════════════════════════════════════════════
    //  DAILY SUMMARY REPORT
    // ═══════════════════════════════════════════════════════════

    /**
     * Daily summary of escalations sent to all supervisors.
     * Runs at 9 AM IST every day.
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    public void sendDailyEscalationSummary() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        LocalDateTime now = LocalDateTime.now();

        List<Complaint> recentEscalations = complaintRepo
            .findByStatusIn(List.of("ESCALATED"))
            .stream()
            .filter(c -> c.getUpdatedAt() != null && c.getUpdatedAt().isAfter(yesterday))
            .toList();

        long totalActive = complaintRepo.countByStatus("ASSIGNED") +
            complaintRepo.countByStatus("IN_PROGRESS") +
            complaintRepo.countByStatus("ESCALATED");

        System.out.println("[DAILY REPORT] Escalations in last 24h: " +
            recentEscalations.size() + " | Total active complaints: " + totalActive);

        // In production, send email digest to all supervisors & admins
    }
}
