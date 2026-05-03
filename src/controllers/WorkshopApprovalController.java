package controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import database.DatabaseConnection;
import dao.AuditDAO;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class WorkshopApprovalController {

    @FXML private TableView<Map<String, Object>> pendingWorkshopsTable;
    @FXML private TableView<Map<String, Object>> approvedWorkshopsTable;

    @FXML private TableColumn<Map<String, Object>, String> workshopNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> ownerNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> licenseColumn;
    @FXML private TableColumn<Map<String, Object>, String> phoneColumn;
    @FXML private TableColumn<Map<String, Object>, String> emailColumn;
    @FXML private TableColumn<Map<String, Object>, String> registeredDateColumn;

    @FXML private TableColumn<Map<String, Object>, String> approvedNameColumn;
    @FXML private TableColumn<Map<String, Object>, String> approvedOwnerColumn;
    @FXML private TableColumn<Map<String, Object>, String> approvedLicenseColumn;
    @FXML private TableColumn<Map<String, Object>, String> approvedPhoneColumn;
    @FXML private TableColumn<Map<String, Object>, String> approvedDateColumn;

    @FXML private Label detailWorkshopName;
    @FXML private Label detailOwnerName;
    @FXML private Label detailLicenseNumber;
    @FXML private Label detailPhone;
    @FXML private Label detailEmail;
    @FXML private Label detailAddress;
    @FXML private Label detailRegisteredDate;

    @FXML private TextArea notesArea;
    @FXML private Label statusLabel;

    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;
    @FXML private Pagination pendingPagination;
    @FXML private Pagination approvedPagination;

    private ObservableList<Map<String, Object>> pendingList;
    private ObservableList<Map<String, Object>> approvedList;
    private List<Map<String, Object>> fullPendingData;
    private List<Map<String, Object>> fullApprovedData;
    private Map<String, Object> selectedWorkshop;
    private AuditDAO auditDAO;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private int currentPendingPage = 0;
    private int currentApprovedPage = 0;
    private int pageSize = 20;

    @FXML
    public void initialize() {
        pendingList = FXCollections.observableArrayList();
        approvedList = FXCollections.observableArrayList();
        auditDAO = new AuditDAO();

        setupTableColumns();
        setupButtonHandlers();
        setupPagination();
        applyVisualEffects();
        loadData();

        statusLabel.setText("Ready");
        pendingWorkshopsTable.setItems(pendingList);
        approvedWorkshopsTable.setItems(approvedList);

        pendingWorkshopsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                selectedWorkshop = newVal;
                displayWorkshopDetails(selectedWorkshop);
            }
        });
    }

    private void setupTableColumns() {
        workshopNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "workshop_name")));
        ownerNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "owner_name")));
        licenseColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "license_number")));
        phoneColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "phone")));
        emailColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "email")));
        registeredDateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "registered_date")));

        approvedNameColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "workshop_name")));
        approvedOwnerColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "owner_name")));
        approvedLicenseColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "license_number")));
        approvedPhoneColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "phone")));
        approvedDateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(getStringValue(cellData.getValue(), "approved_date")));
    }

    private void setupPagination() {
        if (pendingPagination != null) {
            pendingPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentPendingPage = newPage.intValue();
                updatePendingPage();
            });
        }
        if (approvedPagination != null) {
            approvedPagination.currentPageIndexProperty().addListener((obs, old, newPage) -> {
                currentApprovedPage = newPage.intValue();
                updateApprovedPage();
            });
        }
    }

    private void updatePendingPage() {
        if (fullPendingData == null || fullPendingData.isEmpty()) return;
        int start = currentPendingPage * pageSize;
        int end = Math.min(start + pageSize, fullPendingData.size());
        if (start < fullPendingData.size()) {
            pendingList.setAll(fullPendingData.subList(start, end));
        }
    }

    private void updateApprovedPage() {
        if (fullApprovedData == null || fullApprovedData.isEmpty()) return;
        int start = currentApprovedPage * pageSize;
        int end = Math.min(start + pageSize, fullApprovedData.size());
        if (start < fullApprovedData.size()) {
            approvedList.setAll(fullApprovedData.subList(start, end));
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private void setupButtonHandlers() {
        approveButton.setOnAction(event -> handleApprove());
        rejectButton.setOnAction(event -> handleReject());
        refreshButton.setOnAction(event -> {
            loadData();
            statusLabel.setText("Data refreshed");
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> statusLabel.setText("Ready"));
            reset.play();
        });
        backButton.setOnAction(event -> SceneManager.getInstance().switchToAdminView());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        approveButton.setEffect(dropShadow);
        rejectButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        if (fadeButton != null) fadeButton.setEffect(dropShadow);
    }

    private void showFadeAnimation() {
        if (fadeButton != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), fadeButton);
            fadeTransition.setFromValue(1.0);
            fadeTransition.setToValue(0.2);
            fadeTransition.setCycleCount(4);
            fadeTransition.setAutoReverse(true);
            fadeTransition.play();
            statusLabel.setText("Animation played!");
            PauseTransition reset = new PauseTransition(Duration.seconds(2));
            reset.setOnFinished(e -> statusLabel.setText("Ready"));
            reset.play();
        }
    }

    private void loadData() {
        showProgress(true);
        statusLabel.setText("Loading workshop data...");

        try {
            loadPendingWorkshops();
            loadApprovedWorkshops();
            statusLabel.setText("Data loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading data: " + e.getMessage());
            AlertUtil.showError("Load Failed", "Failed to load workshop data.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private void loadPendingWorkshops() {
        pendingList.clear();
        fullPendingData = new ArrayList<>();
        String sql = "SELECT w.id, w.workshop_name, w.license_number, w.phone, w.email, w.address, w.created_at, " +
                "u.full_name as owner_name " +
                "FROM workshops w " +
                "JOIN users u ON w.user_id = u.id " +
                "WHERE w.is_approved = false OR w.is_approved IS NULL " +
                "ORDER BY w.created_at ASC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("workshop_name", rs.getString("workshop_name") != null ? rs.getString("workshop_name") : "N/A");
                row.put("owner_name", rs.getString("owner_name") != null ? rs.getString("owner_name") : "N/A");
                row.put("license_number", rs.getString("license_number") != null ? rs.getString("license_number") : "N/A");
                row.put("phone", rs.getString("phone") != null ? rs.getString("phone") : "N/A");
                row.put("email", rs.getString("email") != null ? rs.getString("email") : "N/A");
                row.put("address", rs.getString("address") != null ? rs.getString("address") : "N/A");
                row.put("registered_date", rs.getTimestamp("created_at") != null ?
                        rs.getTimestamp("created_at").toLocalDateTime().format(formatter) : "N/A");
                fullPendingData.add(row);
            }

            int totalPages = (int) Math.ceil((double) fullPendingData.size() / pageSize);
            if (pendingPagination != null) pendingPagination.setPageCount(Math.max(1, totalPages));
            updatePendingPage();

            if (fullPendingData.isEmpty()) {
                pendingWorkshopsTable.setPlaceholder(new Label("No pending workshops found"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading pending workshops: " + e.getMessage());
        }
    }

    private void loadApprovedWorkshops() {
        approvedList.clear();
        fullApprovedData = new ArrayList<>();
        String sql = "SELECT w.id, w.workshop_name, w.license_number, w.phone, w.email, w.updated_at as approved_date, " +
                "u.full_name as owner_name " +
                "FROM workshops w " +
                "JOIN users u ON w.user_id = u.id " +
                "WHERE w.is_approved = true " +
                "ORDER BY w.updated_at DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("workshop_name", rs.getString("workshop_name") != null ? rs.getString("workshop_name") : "N/A");
                row.put("owner_name", rs.getString("owner_name") != null ? rs.getString("owner_name") : "N/A");
                row.put("license_number", rs.getString("license_number") != null ? rs.getString("license_number") : "N/A");
                row.put("phone", rs.getString("phone") != null ? rs.getString("phone") : "N/A");
                row.put("email", rs.getString("email") != null ? rs.getString("email") : "N/A");
                row.put("approved_date", rs.getTimestamp("approved_date") != null ?
                        rs.getTimestamp("approved_date").toLocalDateTime().format(formatter) : "N/A");
                fullApprovedData.add(row);
            }

            int totalPages = (int) Math.ceil((double) fullApprovedData.size() / pageSize);
            if (approvedPagination != null) approvedPagination.setPageCount(Math.max(1, totalPages));
            updateApprovedPage();

            if (fullApprovedData.isEmpty()) {
                approvedWorkshopsTable.setPlaceholder(new Label("No approved workshops found"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading approved workshops: " + e.getMessage());
        }
    }

    private void displayWorkshopDetails(Map<String, Object> workshop) {
        detailWorkshopName.setText(getStringValue(workshop, "workshop_name"));
        detailOwnerName.setText(getStringValue(workshop, "owner_name"));
        detailLicenseNumber.setText(getStringValue(workshop, "license_number"));
        detailPhone.setText(getStringValue(workshop, "phone"));
        detailEmail.setText(getStringValue(workshop, "email"));
        detailAddress.setText(getStringValue(workshop, "address"));
        detailRegisteredDate.setText(getStringValue(workshop, "registered_date"));
    }

    private void handleApprove() {
        if (selectedWorkshop == null) {
            AlertUtil.showWarning("No Selection", "Please select a workshop to approve.");
            return;
        }

        int workshopId = (Integer) selectedWorkshop.get("id");
        String workshopName = getStringValue(selectedWorkshop, "workshop_name");
        String notes = notesArea.getText().trim();

        boolean confirmed = AlertUtil.showConfirmation("Approve Workshop",
                "Approve workshop '" + workshopName + "'?\n\nThis will allow the workshop to access the system.");

        if (confirmed) {
            showProgress(true);
            statusLabel.setText("Approving workshop...");
            updateProgress(0.3);

            try {
                String sql = "UPDATE workshops SET is_approved = true, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, workshopId);
                    int result = ps.executeUpdate();
                    updateProgress(0.8);

                    if (result > 0) {
                        updateProgress(1.0);

                        int currentUserId = SessionManager.getInstance().getUserId();
                        auditDAO.logAction(currentUserId, "APPROVE_WORKSHOP: " + workshopName + " (ID: " + workshopId + ")", "127.0.0.1");

                        AlertUtil.showSuccess("Workshop Approved", "Workshop '" + workshopName + "' has been approved.");
                        statusLabel.setText("Workshop approved successfully");
                        loadData();
                        clearDetails();
                    } else {
                        AlertUtil.showError("Approval Failed", "Could not approve workshop.");
                        statusLabel.setText("Approval failed");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Database Error", "Failed to approve workshop: " + e.getMessage());
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    private void handleReject() {
        if (selectedWorkshop == null) {
            AlertUtil.showWarning("No Selection", "Please select a workshop to reject.");
            return;
        }

        int workshopId = (Integer) selectedWorkshop.get("id");
        String workshopName = getStringValue(selectedWorkshop, "workshop_name");
        String notes = notesArea.getText().trim();

        if (notes.isEmpty()) {
            AlertUtil.showWarning("Reason Required", "Please provide a reason for rejection.");
            notesArea.requestFocus();
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Reject Workshop",
                "Reject workshop '" + workshopName + "'?\n\nThis will delete the workshop registration.");

        if (confirmed) {
            showProgress(true);
            statusLabel.setText("Rejecting workshop...");
            updateProgress(0.3);

            try {
                String getUserSql = "SELECT user_id FROM workshops WHERE id = ?";
                int userId = -1;
                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                     PreparedStatement ps = conn.prepareStatement(getUserSql)) {
                    ps.setInt(1, workshopId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        userId = rs.getInt("user_id");
                    }
                }

                String deleteWorkshopSql = "DELETE FROM workshops WHERE id = ?";
                try (Connection conn = DatabaseConnection.getInstance().getConnection();
                     PreparedStatement ps = conn.prepareStatement(deleteWorkshopSql)) {
                    ps.setInt(1, workshopId);
                    ps.executeUpdate();
                }

                if (userId > 0) {
                    String deleteUserSql = "DELETE FROM users WHERE id = ?";
                    try (Connection conn = DatabaseConnection.getInstance().getConnection();
                         PreparedStatement ps = conn.prepareStatement(deleteUserSql)) {
                        ps.setInt(1, userId);
                        ps.executeUpdate();
                    }
                }

                updateProgress(0.8);
                updateProgress(1.0);

                int currentUserId = SessionManager.getInstance().getUserId();
                auditDAO.logAction(currentUserId, "REJECT_WORKSHOP: " + workshopName + " (ID: " + workshopId + ") - Reason: " + notes, "127.0.0.1");

                AlertUtil.showSuccess("Workshop Rejected", "Workshop '" + workshopName + "' has been rejected.");
                statusLabel.setText("Workshop rejected");
                loadData();
                clearDetails();

            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Error: " + e.getMessage());
                AlertUtil.showError("Database Error", "Failed to reject workshop: " + e.getMessage());
            } finally {
                hideProgressAfterDelay();
            }
        }
    }

    private void clearDetails() {
        detailWorkshopName.setText("");
        detailOwnerName.setText("");
        detailLicenseNumber.setText("");
        detailPhone.setText("");
        detailEmail.setText("");
        detailAddress.setText("");
        detailRegisteredDate.setText("");
        notesArea.clear();
        selectedWorkshop = null;
        pendingWorkshopsTable.getSelectionModel().clearSelection();
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    private void updateProgress(double progress) {
        if (operationProgress != null) operationProgress.setProgress(progress);
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(event -> {
            if (loadProgress != null) loadProgress.setVisible(false);
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
        });
        delay.play();
    }
}