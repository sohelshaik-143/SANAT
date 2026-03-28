package com.civicguard.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * CivicGuard-AI Officer Desktop Application
 * ==========================================
 * JavaFX application for government officers to:
 * - View assigned complaints on a dashboard
 * - Inspect complaint details with images and AI verification results
 * - Upload resolution photos (before/after)
 * - Track escalation status
 * - Generate audit-ready reports (PDF)
 *
 * Connects to the Spring Boot backend via REST API.
 */
public class MainApp extends Application {

    private static final String APP_TITLE = "CivicGuard-AI — Officer Dashboard";
    private static final String PRIMARY_COLOR = "#0d9488";   // Teal
    private static final String DARK_BG = "#0f172a";         // Slate 900
    private static final String CARD_BG = "#1e293b";         // Slate 800

    private BorderPane rootLayout;
    private VBox sideNav;
    private StackPane contentArea;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(750);

        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: " + DARK_BG + ";");

        // ─── Side Navigation ──────────────────────────────────
        sideNav = createSideNav();
        rootLayout.setLeft(sideNav);

        // ─── Top Bar ──────────────────────────────────────────
        HBox topBar = createTopBar();
        rootLayout.setTop(topBar);

        // ─── Content Area ─────────────────────────────────────
        contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));
        contentArea.setAlignment(Pos.TOP_LEFT);
        showDashboard(); // Default view
        rootLayout.setCenter(contentArea);

        Scene scene = new Scene(rootLayout, 1400, 850);
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("""
            ╔══════════════════════════════════════════╗
            ║  🛡️ CivicGuard-AI Officer Desktop       ║
            ║  Connected to: http://localhost:8080     ║
            ╚══════════════════════════════════════════╝
        """);
    }

    // ═══════════════════════════════════════════════════════════
    //  NAVIGATION
    // ═══════════════════════════════════════════════════════════

    private VBox createSideNav() {
        VBox nav = new VBox(5);
        nav.setPrefWidth(220);
        nav.setPadding(new Insets(20, 10, 20, 10));
        nav.setStyle("-fx-background-color: " + CARD_BG + ";");

        // Logo / Title
        Label logo = new Label("🛡️ CivicGuard");
        logo.setFont(Font.font("System", FontWeight.BOLD, 18));
        logo.setTextFill(Color.web(PRIMARY_COLOR));
        logo.setPadding(new Insets(0, 0, 20, 5));

        // Navigation buttons
        Button btnDashboard = createNavButton("📊  Dashboard");
        Button btnAssigned = createNavButton("📋  My Complaints");
        Button btnEscalated = createNavButton("⚠️  Escalated");
        Button btnResolve = createNavButton("✅  Submit Resolution");
        Button btnReports = createNavButton("📄  Generate Report");
        Button btnFraud = createNavButton("🔍  Fraud Alerts");
        Button btnSettings = createNavButton("⚙️  Settings");

        btnDashboard.setOnAction(e -> showDashboard());
        btnAssigned.setOnAction(e -> showAssignedComplaints());
        btnEscalated.setOnAction(e -> showEscalatedComplaints());
        btnResolve.setOnAction(e -> showResolutionForm());
        btnReports.setOnAction(e -> showReportGenerator());
        btnFraud.setOnAction(e -> showFraudAlerts());

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Officer info
        Label officerInfo = new Label("Logged in as:\nField Officer");
        officerInfo.setTextFill(Color.web("#94a3b8"));
        officerInfo.setFont(Font.font(11));

        nav.getChildren().addAll(
            logo,
            btnDashboard, btnAssigned, btnEscalated,
            new Separator(),
            btnResolve, btnReports, btnFraud,
            spacer,
            new Separator(),
            btnSettings, officerInfo
        );

        return nav;
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 15, 10, 15));
        btn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #e2e8f0; " +
            "-fx-font-size: 13; " +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: " + PRIMARY_COLOR + "22; " +
            "-fx-text-fill: " + PRIMARY_COLOR + "; " +
            "-fx-font-size: 13; " +
            "-fx-cursor: hand; " +
            "-fx-background-radius: 8;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #e2e8f0; " +
            "-fx-font-size: 13; " +
            "-fx-cursor: hand;"
        ));
        return btn;
    }

    private HBox createTopBar() {
        HBox bar = new HBox(15);
        bar.setPadding(new Insets(12, 20, 12, 20));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: " + CARD_BG + "; " +
                     "-fx-border-color: #334155; -fx-border-width: 0 0 1 0;");

        Label title = new Label("Officer Command Center");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label stats = new Label("🟢 12 Active  |  🔴 3 Overdue  |  ⚡ 2 Escalated");
        stats.setTextFill(Color.web("#94a3b8"));
        stats.setFont(Font.font(12));

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setStyle(
            "-fx-background-color: " + PRIMARY_COLOR + "; " +
            "-fx-text-fill: white; -fx-font-size: 12; " +
            "-fx-background-radius: 6; -fx-cursor: hand;"
        );

        bar.getChildren().addAll(title, spacer, stats, refreshBtn);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════
    //  CONTENT VIEWS
    // ═══════════════════════════════════════════════════════════

    private void showDashboard() {
        contentArea.getChildren().clear();

        VBox dashboard = new VBox(20);
        dashboard.setPadding(new Insets(10));

        // Stats cards row
        HBox statsRow = new HBox(15);
        statsRow.getChildren().addAll(
            createStatCard("Total Assigned", "47", "#3b82f6"),
            createStatCard("Resolved", "38", "#22c55e"),
            createStatCard("Pending", "6", "#f59e0b"),
            createStatCard("Escalated", "3", "#ef4444")
        );

        // Complaints table placeholder
        Label tableTitle = new Label("Recent Complaints");
        tableTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        tableTitle.setTextFill(Color.WHITE);

        TableView<String> table = new TableView<>();
        table.setStyle("-fx-background-color: " + CARD_BG + ";");
        table.setPrefHeight(400);
        table.setPlaceholder(new Label("Loading complaints from server..."));

        TableColumn<String, String> colTicket = new TableColumn<>("Ticket #");
        colTicket.setPrefWidth(140);
        TableColumn<String, String> colCategory = new TableColumn<>("Category");
        colCategory.setPrefWidth(150);
        TableColumn<String, String> colSeverity = new TableColumn<>("Severity");
        colSeverity.setPrefWidth(100);
        TableColumn<String, String> colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(120);
        TableColumn<String, String> colDeadline = new TableColumn<>("Deadline");
        colDeadline.setPrefWidth(150);
        TableColumn<String, String> colArea = new TableColumn<>("Area/Pincode");
        colArea.setPrefWidth(130);

        table.getColumns().addAll(colTicket, colCategory, colSeverity,
                                   colStatus, colDeadline, colArea);

        dashboard.getChildren().addAll(statsRow, tableTitle, table);
        contentArea.getChildren().add(dashboard);
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setPrefWidth(200);
        card.setStyle(
            "-fx-background-color: " + CARD_BG + "; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: " + color + "44; " +
            "-fx-border-width: 0 0 3 0; " +
            "-fx-border-radius: 10;"
        );

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web("#94a3b8"));
        titleLabel.setFont(Font.font(12));

        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.web(color));
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 28));

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private void showAssignedComplaints() {
        contentArea.getChildren().clear();
        Label label = new Label("📋 Assigned Complaints — Loading from API...");
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font(16));
        contentArea.getChildren().add(label);
    }

    private void showEscalatedComplaints() {
        contentArea.getChildren().clear();
        Label label = new Label("⚠️ Escalated Complaints — Loading...");
        label.setTextFill(Color.web("#ef4444"));
        label.setFont(Font.font(16));
        contentArea.getChildren().add(label);
    }

    private void showResolutionForm() {
        contentArea.getChildren().clear();

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setMaxWidth(600);
        form.setStyle("-fx-background-color: " + CARD_BG + "; -fx-background-radius: 12;");

        Label title = new Label("✅ Submit Resolution");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setTextFill(Color.web(PRIMARY_COLOR));

        TextField ticketField = new TextField();
        ticketField.setPromptText("Enter Ticket Number (e.g., CG-2026-TS-001042)");
        ticketField.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-prompt-text-fill: #64748b;");

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Resolution notes...");
        notesArea.setPrefRowCount(4);
        notesArea.setStyle("-fx-background-color: #334155; -fx-text-fill: white; -fx-prompt-text-fill: #64748b;");

        Button uploadBtn = new Button("📷 Upload Resolution Photo");
        uploadBtn.setStyle("-fx-background-color: #334155; -fx-text-fill: #e2e8f0; " +
                           "-fx-font-size: 13; -fx-background-radius: 8; -fx-cursor: hand;");

        Button submitBtn = new Button("Submit Resolution for AI Verification");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setStyle(
            "-fx-background-color: " + PRIMARY_COLOR + "; " +
            "-fx-text-fill: white; -fx-font-size: 14; " +
            "-fx-font-weight: bold; -fx-background-radius: 8; " +
            "-fx-cursor: hand; -fx-padding: 12;"
        );

        form.getChildren().addAll(title, ticketField, uploadBtn, notesArea, submitBtn);
        contentArea.getChildren().add(form);
    }

    private void showReportGenerator() {
        contentArea.getChildren().clear();
        Label label = new Label("📄 Report Generator — Select a complaint to generate PDF...");
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font(16));
        contentArea.getChildren().add(label);
    }

    private void showFraudAlerts() {
        contentArea.getChildren().clear();
        Label label = new Label("🔍 Fraud Alerts — No active fraud flags");
        label.setTextFill(Color.web("#f59e0b"));
        label.setFont(Font.font(16));
        contentArea.getChildren().add(label);
    }

    // ═══════════════════════════════════════════════════════════
    //  MAIN
    // ═══════════════════════════════════════════════════════════

    public static void main(String[] args) {
        launch(args);
    }
}
