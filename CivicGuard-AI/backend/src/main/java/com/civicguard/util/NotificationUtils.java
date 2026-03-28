package com.civicguard.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Notification utility for sending SMS and Email alerts.
 * Handles all citizen, officer, and escalation notifications.
 *
 * SMS Integration: Twilio / MSG91 (India)
 * Email: Spring Boot JavaMailSender
 *
 * All notifications are sent asynchronously to avoid blocking API responses.
 */
@Component
public class NotificationUtils {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:civicguard@gov.in}")
    private String fromEmail;

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    public NotificationUtils(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ═══════════════════════════════════════════════════════════
    //  CITIZEN NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Send confirmation after complaint submission.
     */
    @Async
    public void sendComplaintConfirmation(String phone, String email,
                                          String ticketNumber, String category) {
        // SMS
        String smsMessage = String.format(
            "CivicGuard: Your complaint has been registered.\n" +
            "Ticket: %s\nCategory: %s\n" +
            "Track at: civicguard.gov.in/track/%s",
            ticketNumber, category, ticketNumber
        );
        sendSMS(phone, smsMessage);

        // Email
        String emailBody = String.format("""
            Dear Citizen,
            
            Your civic complaint has been successfully registered with CivicGuard-AI.
            
            Ticket Number: %s
            Issue Category: %s
            Status: Verified & Assigned
            
            You can track your complaint at:
            https://civicguard.gov.in/track/%s
            
            Our AI system has verified your submission and automatically routed 
            it to the appropriate government department. You will receive updates 
            via SMS as your complaint progresses.
            
            Important Helplines:
            - Cyber Crime Helpline: 1930
            - Online Portal: cybercrime.gov.in
            - CERT-IN: incident@cert-in.org.in
            
            Thank you for helping improve civic infrastructure.
            
            Team CivicGuard-AI
            Ministry of Electronics & IT, Government of India
            """, ticketNumber, category, ticketNumber);

        sendEmail(email, "CivicGuard Complaint Registered - " + ticketNumber, emailBody);
    }

    /**
     * Notify citizen when resolution status changes.
     */
    @Async
    public void sendResolutionUpdate(String phone, String ticketNumber, String status) {
        String statusMessage = switch (status) {
            case "RESOLVED" -> "has been RESOLVED. The issue has been verified as fixed by our AI system.";
            case "FRAUD_DETECTED" -> "resolution was flagged by AI for potential fraud. The case has been escalated.";
            case "RESOLUTION_SUBMITTED" -> "resolution has been submitted by the officer and is under AI verification.";
            default -> "status has been updated to: " + status;
        };

        String sms = String.format(
            "CivicGuard Update: Your complaint %s %s\nTrack: civicguard.gov.in/track/%s",
            ticketNumber, statusMessage, ticketNumber
        );
        sendSMS(phone, sms);
    }

    /**
     * Notify citizen about escalation.
     */
    @Async
    public void sendEscalationNotification(String phone, String ticketNumber,
                                            int level, String designation) {
        String sms = String.format(
            "CivicGuard: Your complaint %s has been escalated to Level %d (%s) " +
            "due to non-response. Rest assured, action will be taken.\n" +
            "Track: civicguard.gov.in/track/%s",
            ticketNumber, level, designation, ticketNumber
        );
        sendSMS(phone, sms);
    }

    // ═══════════════════════════════════════════════════════════
    //  OFFICER NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Notify officer about new complaint assignment.
     */
    @Async
    public void sendOfficerAssignment(String phone, String email,
                                       String ticketNumber, String category) {
        String sms = String.format(
            "CivicGuard ALERT: New complaint assigned to you.\n" +
            "Ticket: %s\nCategory: %s\n" +
            "Login to your dashboard to view details and take action.",
            ticketNumber, category
        );
        sendSMS(phone, sms);
        sendEmail(email, "New Complaint Assignment - " + ticketNumber,
            "A new civic complaint has been assigned to you.\n\n" +
            "Ticket: " + ticketNumber + "\nCategory: " + category +
            "\n\nPlease log in to the CivicGuard officer portal to view details.");
    }

    /**
     * Alert the next-tier authority about an escalation.
     */
    @Async
    public void sendEscalationAlert(String phone, String email,
                                     String ticketNumber, int level,
                                     String category, String severity) {
        String sms = String.format(
            "URGENT CivicGuard ESCALATION (Level %d):\n" +
            "Ticket: %s\nCategory: %s\nSeverity: %s\n" +
            "Previous officer failed to act within SLA. Immediate action required.",
            level, ticketNumber, category, severity
        );
        sendSMS(phone, sms);
        sendEmail(email, "ESCALATION ALERT Level " + level + " - " + ticketNumber,
            "An escalated civic complaint requires your immediate attention.\n\n" +
            "Ticket: " + ticketNumber + "\nSeverity: " + severity +
            "\nEscalation Level: " + level +
            "\n\nThe previous officer did not resolve this within the SLA deadline.");
    }

    /**
     * Warn an officer about escalation from their assignment.
     */
    @Async
    public void sendEscalationWarning(String phone, String email,
                                       String ticketNumber, int newLevel) {
        String sms = String.format(
            "CivicGuard WARNING: Complaint %s has been escalated to Level %d " +
            "due to your non-action. This has been recorded in your performance metrics.",
            ticketNumber, newLevel
        );
        sendSMS(phone, sms);
    }

    // ═══════════════════════════════════════════════════════════
    //  TRANSPORT METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Send SMS via configured provider (Twilio/MSG91).
     * In development mode, logs to console.
     */
    private void sendSMS(String phone, String message) {
        if (!smsEnabled || phone == null || phone.isEmpty()) {
            System.out.println("[SMS-DEV] To: " + phone + " | " + message);
            return;
        }

        try {
            // Production: Integrate Twilio or MSG91 here
            // TwilioService.sendSMS(phone, message);
            System.out.println("[SMS] Sent to " + phone);
        } catch (Exception e) {
            System.err.println("[SMS] Failed to send to " + phone + ": " + e.getMessage());
        }
    }

    /**
     * Send email notification.
     */
    private void sendEmail(String to, String subject, String body) {
        if (to == null || to.isEmpty()) return;

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromEmail);
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);
            System.out.println("[EMAIL] Sent to " + to);
        } catch (Exception e) {
            System.err.println("[EMAIL] Failed to send to " + to + ": " + e.getMessage());
        }
    }
}
