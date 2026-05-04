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
import dao.WorkshopDAO;
import dao.AuditDAO;
import models.Workshop;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WorkshopApprovalController {

    @FXML private TableView<Workshop> pendingWorkshopsTable;
    @FXML private TableView<Workshop> approvedWorkshopsTable;

    @FXML private TableColumn<Workshop, String> workshopNameColumn;
    @FXML private TableColumn<Workshop, String> ownerNameColumn;
    @FXML private TableColumn<Workshop, String> licenseColumn;
    @FXML private TableColumn<Workshop, String> phoneColumn;
    @FXML private TableColumn<Workshop, String> emailColumn;
    @FXML private TableColumn<Workshop, String> registeredDateColumn;

    @FXML private TableColumn<Workshop, String> approvedNameColumn;
    @FXML private TableColumn<Workshop, String> approvedOwnerColumn;
    @FXML private TableColumn<Workshop, String> approvedLicenseColumn;
    @FXML private TableColumn<Workshop, String> approvedPhoneColumn;
    @FXML private TableColumn<Workshop, String> approvedDateColumn;

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

    private WorkshopDAO workshopDAO;
    private AuditDAO auditDAO;
    private ObservableList<Workshop> pendingList;
    private ObservableList<Workshop> approvedList;
    private List<Workshop> fullPendingData;
    private List<Workshop> fullApprovedData;
    private Workshop selectedWorkshop;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private int currentPendingPage = 0;
    private int currentApprovedPage = 0;
    private int pageSize = 20;

    @FXML
    public void initialize() {
        workshopDAO = new WorkshopDAO();
        auditDAO = new AuditDAO();
        pendingList = FXCollections.observableArrayList();
        approvedList = FXCollections.observableArrayList();

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
        workshopNameColumn.setCellValueFactory(cellData -> cellData.getValue().workshopNameProperty());
        ownerNameColumn.setCellValueFactory(cellData -> cellData.getValue().ownerNameProperty());
        licenseColumn.setCellValueFactory(cellData -> cellData.getValue().licenseNumberProperty());
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        registeredDateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatDateTime(cellData.getValue().getCreatedAt())));

        approvedNameColumn.setCellValueFactory(cellData -> cellData.getValue().workshopNameProperty());
        approvedOwnerColumn.setCellValueFactory(cellData -> cellData.getValue().ownerNameProperty());
        approvedLicenseColumn.setCellValueFactory(cellData -> cellData.getValue().licenseNumberProperty());
        approvedPhoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneProperty());
        approvedDateColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(formatDateTime(cellData.getValue().getUpdatedAt())));
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
        if (fadeButton != null) fadeButton.OnAction(event -> showFadeAnimation());
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

    /**
     * Load pending workshops using WorkshopDAO (which uses views)
     */
    private void loadPendingWorkshops() throws Exception {
        List<Workshop> allWorkshops = workshopDAO.findAll();
        fullPendingData = new java.util.ArrayList<>();

        for (Workshop workshop : allWorkshops) {
            if (!workshop.isApproved()) {
                fullPendingData.add(workshop);
            }
        }

        System.out.println("Found " + fullPendingData.size() + " pending workshops");

        int totalPages = (int) Math.ceil((double) fullPendingData.size() / pageSize);
        if (pendingPagination != null) pendingPagination.setPageCount(Math.max(1, totalPages));
        updatePendingPage();

        if (fullPendingData.isEmpty()) {
            pendingWorkshopsTable.setPlaceholder(new Label("No pending workshops found"));
        }
    }

    /**
     * Load approved workshops using WorkshopDAO (which uses views)
     */
    private void loadApprovedWorkshops() throws Exception {
        List<Workshop> allWorkshops = workshopDAO.findAll();
        fullApprovedData = new java.util.ArrayList<>();

        for (Workshop workshop : allWorkshops) {
            if (workshop.isApproved()) {
                fullApprovedData.add(workshop);
            }
        }

        System.out.println("Found " + fullApprovedData.size() + " approved workshops");

        int totalPages = (int) Math.ceil((double) fullApprovedData.size() / pageSize);
        if (approvedPagination != null) approvedPagination.setPageCount(Math.max(1, totalPages));
        updateApprovedPage();

        if (fullApprovedData.isEmpty()) {
            approvedWorkshopsTable.setPlaceholder(new Label("No approved workshops found"));
        }
    }

    private void displayWorkshopDetails(Workshop workshop) {
        detailWorkshopName.setText(workshop.getWorkshopName());
        detailOwnerName.setText(workshop.getOwnerName());
        detailLicenseNumber.setText(workshop.getLicenseNumber());
        detailPhone.setText(workshop.getPhone());
        detailEmail.setText(workshop.getEmail());
        detailAddress.setText(workshop.getAddress());
        detailRegisteredDate.setText(formatDateTime(workshop.getCreatedAt()));
    }

    private void handleApprove() {
        if (selectedWorkshop == null) {
            AlertUtil.showWarning("No Selection", "Please select a workshop to approve.");
            return;
        }

        String workshopName = selectedWorkshop.getWorkshopName();

        boolean confirmed = AlertUtil.showConfirmation("Approve Workshop",
                "Approve workshop '" + workshopName + "'?\n\nThis will allow the workshop to access the system.");

        if (confirmed) {
            showProgress(true);
            statusLabel.setText("Approving workshop...");
            updateProgress(0.3);

            try {
                // Use WorkshopDAO to update approval status
                // Note: You may need to add an approveWorkshop method to WorkshopDAO
                boolean success = workshopDAO.approveWorkshop(selectedWorkshop.getId());
                updateProgress(0.8);

                if (success) {
                    updateProgress(1.0);

                    int currentUserId = SessionManager.getInstance().getUserId();
                    auditDAO.logAction(currentUserId, "APPROVE_WORKSHOP: " + workshopName + " (ID: " + selectedWorkshop.getId() + ")", "127.0.0.1");

                    AlertUtil.showSuccess("Workshop Approved", "Workshop '" + workshopName + "' has been approved.");
                    statusLabel.setText("Workshop approved successfully");
                    loadData();
                    clearDetails();
                } else {
                    AlertUtil.showError("Approval Failed", "Could not approve workshop.");
                    statusLabel.setText("Approval failed");
                }
            } catch (Exception e) {
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

        String workshopName = selectedWorkshop.getWorkshopName();
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
                boolean success = workshopDAO.delete(selectedWorkshop.getId());
                updateProgress(0.8);
                updateProgress(1.0);

                int currentUserId = SessionManager.getInstance().getUserId();
                auditDAO.logAction(currentUserId, "REJECT_WORKSHOP: " + workshopName + " (ID: " + selectedWorkshop.getId() + ") - Reason: " + notes, "127.0.0.1");

                AlertUtil.showSuccess("Workshop Rejected", "Workshop '" + workshopName + "' has been rejected.");
                statusLabel.setText("Workshop rejected");
                loadData();
                clearDetails();

            } catch (Exception e) {
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

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(formatter);
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