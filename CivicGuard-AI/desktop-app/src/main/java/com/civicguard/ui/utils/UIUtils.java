package com.civicguard.ui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * UI utility methods for the CivicGuard officer desktop application.
 */
public class UIUtils {

    private static final DateTimeFormatter DISPLAY_FORMAT =
        DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    /**
     * Show an information alert dialog.
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show an error alert dialog.
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show a confirmation dialog.
     */
    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait()
            .filter(response -> response == javafx.scene.control.ButtonType.OK)
            .isPresent();
    }

    /**
     * Open a file chooser for image selection.
     */
    public static File chooseImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Resolution Photo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png", "*.webp"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        return fileChooser.showOpenDialog(stage);
    }

    /**
     * Format a datetime for display.
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_FORMAT) : "—";
    }

    /**
     * Get a severity badge CSS class.
     */
    public static String getSeverityClass(String severity) {
        return switch (severity != null ? severity : "") {
            case "EMERGENCY" -> "badge-emergency";
            case "CRITICAL" -> "badge-critical";
            case "HIGH" -> "badge-high";
            case "MEDIUM" -> "badge-medium";
            case "LOW" -> "badge-low";
            default -> "badge-medium";
        };
    }

    /**
     * Get a status display color.
     */
    public static String getStatusColor(String status) {
        return switch (status != null ? status : "") {
            case "SUBMITTED" -> "#94a3b8";
            case "VERIFIED" -> "#3b82f6";
            case "ASSIGNED" -> "#8b5cf6";
            case "IN_PROGRESS" -> "#f59e0b";
            case "ESCALATED" -> "#ef4444";
            case "RESOLVED" -> "#22c55e";
            case "REJECTED" -> "#64748b";
            case "FRAUD_DETECTED" -> "#dc2626";
            default -> "#94a3b8";
        };
    }
}
