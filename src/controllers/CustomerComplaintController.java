package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import utils.ValidationUtil;
import dao.CustomerComplaintDAO;
import dao.WorkshopDAO;
import models.CustomerComplaint;
import models.Workshop;

import java.util.List;

public class CustomerComplaintController {

    @FXML private TableView<CustomerComplaint> complaintsTable;
    @FXML private TableColumn<CustomerComplaint, String> workshopColumn;
    @FXML private TableColumn<CustomerComplaint, String> complaintDateColumn;
    @FXML private TableColumn<CustomerComplaint, String> complaintTextColumn;
    @FXML private TableColumn<CustomerComplaint, String> statusColumn;

    @FXML private ComboBox<Workshop> workshopComboBox;
    @FXML private TextArea complaintTextArea;
    @FXML private TextArea resolutionNotesArea;
    @FXML private TextField statusField;
    @FXML private Label complaintDateLabel;

    @FXML private Button submitButton;
    @FXML private Button resolveButton;
    @FXML private Button dismissButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private CustomerComplaintDAO complaintDAO;
    private WorkshopDAO workshopDAO;
    private CustomerComplaint selectedComplaint;
    private int customerId;

    @FXML
    public void initialize() {
        complaintDAO = new CustomerComplaintDAO();
        workshopDAO = new WorkshopDAO();

        customerId = SessionManager.getInstance().getCustomerId();

        setupTableColumns();
        loadWorkshops();
        loadComplaints();
        setupButtonHandlers();
        setupTableSelection();

        boolean isAdminOrWorkshop = SessionManager.getInstance().isAdmin() || SessionManager.getInstance().isWorkshop();
        resolveButton.setVisible(isAdminOrWorkshop);
        dismissButton.setVisible(isAdminOrWorkshop);
        resolutionNotesArea.setEditable(isAdminOrWorkshop);
    }

    private void setupTableColumns() {
        workshopColumn.setCellValueFactory(cellData -> cellData.getValue().workshopNameProperty());
        complaintDateColumn.setCellValueFactory(cellData -> cellData.getValue().complaintDateProperty().asString());
        complaintTextColumn.setCellValueFactory(cellData -> cellData.getValue().complaintTextProperty());
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().resolutionStatusProperty());
    }

    private void loadWorkshops() {
        try {
            List<Workshop> workshops = workshopDAO.findApprovedWorkshops();
            workshopComboBox.getItems().setAll(workshops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadComplaints() {
        try {
            List<CustomerComplaint> complaints;
            if (SessionManager.getInstance().isAdmin() || SessionManager.getInstance().isWorkshop()) {
                complaints = complaintDAO.findAll();
            } else {
                complaints = complaintDAO.findByCustomerId(customerId);
            }
            complaintsTable.getItems().setAll(complaints);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load complaints.");
        }
    }

    private void setupButtonHandlers() {
        submitButton.setOnAction(event -> handleSubmit());
        resolveButton.setOnAction(event -> handleResolve());
        dismissButton.setOnAction(event -> handleDismiss());
        refreshButton.setOnAction(event -> loadComplaints());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToCustomerProfileView());
    }

    private void setupTableSelection() {
        complaintsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedComplaint = newSelection;
                displayComplaintDetails(selectedComplaint);
            }
        });
    }

    private void displayComplaintDetails(CustomerComplaint complaint) {
        if (complaint.getWorkshopId() > 0) {
            try {
                Workshop workshop = workshopDAO.findById(complaint.getWorkshopId());
                workshopComboBox.getSelectionModel().select(workshop);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        complaintTextArea.setText(complaint.getComplaintText());
        if (complaint.getResolutionNotes() != null) {
            resolutionNotesArea.setText(complaint.getResolutionNotes());
        }
        statusField.setText(complaint.getResolutionStatus());

        if (complaint.getComplaintDate() != null) {
            complaintDateLabel.setText(complaint.getComplaintDate().toString());
        }

        boolean isPending = "PENDING".equals(complaint.getResolutionStatus());
        resolveButton.setDisable(!isPending);
        dismissButton.setDisable(!isPending);
    }

    private void handleSubmit() {
        Workshop selectedWorkshop = workshopComboBox.getSelectionModel().getSelectedItem();

        if (selectedWorkshop == null) {
            AlertUtil.showWarning("Validation Error", "Please select a workshop.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(complaintTextArea.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter your complaint.");
            complaintTextArea.requestFocus();
            return;
        }

        try {
            CustomerComplaint complaint = new CustomerComplaint();
            complaint.setCustomerId(customerId);
            complaint.setWorkshopId(selectedWorkshop.getId());
            complaint.setComplaintText(complaintTextArea.getText().trim());

            boolean success = complaintDAO.insert(complaint);

            if (success) {
                AlertUtil.showSuccess("Complaint submitted successfully.");
                clearForm();
                loadComplaints();
            } else {
                AlertUtil.showError("Submit Failed", "Failed to submit complaint.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while submitting complaint.");
        }
    }

    private void handleResolve() {
        if (selectedComplaint == null) {
            AlertUtil.showWarning("No Selection", "Please select a complaint to resolve.");
            return;
        }

        if (!"PENDING".equals(selectedComplaint.getResolutionStatus())) {
            AlertUtil.showWarning("Already Processed", "This complaint has already been processed.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Resolve Complaint", "Mark this complaint as resolved?");

        if (confirmed) {
            try {
                boolean success = complaintDAO.updateStatus(selectedComplaint.getId(), "RESOLVED", resolutionNotesArea.getText());

                if (success) {
                    AlertUtil.showSuccess("Complaint resolved successfully.");
                    loadComplaints();
                } else {
                    AlertUtil.showError("Resolve Failed", "Failed to resolve complaint.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "An error occurred while resolving complaint.");
            }
        }
    }

    private void handleDismiss() {
        if (selectedComplaint == null) {
            AlertUtil.showWarning("No Selection", "Please select a complaint to dismiss.");
            return;
        }

        if (!"PENDING".equals(selectedComplaint.getResolutionStatus())) {
            AlertUtil.showWarning("Already Processed", "This complaint has already been processed.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Dismiss Complaint", "Are you sure you want to dismiss this complaint?");

        if (confirmed) {
            try {
                boolean success = complaintDAO.updateStatus(selectedComplaint.getId(), "DISMISSED", resolutionNotesArea.getText());

                if (success) {
                    AlertUtil.showSuccess("Complaint dismissed successfully.");
                    loadComplaints();
                } else {
                    AlertUtil.showError("Dismiss Failed", "Failed to dismiss complaint.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Database Error", "An error occurred while dismissing complaint.");
            }
        }
    }

    private void clearForm() {
        workshopComboBox.getSelectionModel().clearSelection();
        complaintTextArea.clear();
        resolutionNotesArea.clear();
        statusField.clear();
        complaintDateLabel.setText("");
        selectedComplaint = null;
        complaintsTable.getSelectionModel().clearSelection();
    }
}