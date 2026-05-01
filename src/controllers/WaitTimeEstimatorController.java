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
import utils.DateUtil;
import dao.WorkshopDAO;
import dao.ServiceRecordDAO;
import models.Workshop;
import models.ServiceRecord;

import java.time.LocalDate;
import java.util.List;

public class WaitTimeEstimatorController {

    @FXML private ComboBox<Workshop> workshopComboBox;
    @FXML private Button estimateButton;
    @FXML private Button backButton;
    @FXML private Button refreshButton;
    @FXML private Button bookButton;
    @FXML private Button fadeButton;

    @FXML private Label workshopNameLabel;
    @FXML private Label activeServicesLabel;
    @FXML private Label busyLevelLabel;
    @FXML private Label estimatedWaitTimeLabel;
    @FXML private Label recommendationLabel;
    @FXML private Label statusLabel;
    @FXML private Label mechanicsAvailableLabel;
    @FXML private Label queueLengthLabel;

    @FXML private ProgressBar busyProgressBar;
    @FXML private ProgressBar waitTimeProgress;
    @FXML private ProgressIndicator loadProgress;
    @FXML private ProgressBar operationProgress;

    @FXML private TableView<QueueItem> queueTable;
    @FXML private TableColumn<QueueItem, String> vehicleColumn;
    @FXML private TableColumn<QueueItem, String> serviceTypeColumn;
    @FXML private TableColumn<QueueItem, String> estimatedDurationColumn;

    private WorkshopDAO workshopDAO;
    private ServiceRecordDAO serviceDAO;
    private int customerId;
    private ObservableList<QueueItem> queueList;

    @FXML
    public void initialize() {
        workshopDAO = new WorkshopDAO();
        serviceDAO = new ServiceRecordDAO();
        queueList = FXCollections.observableArrayList();

        customerId = SessionManager.getInstance().getCustomerId();

        setupTableColumns();
        loadWorkshops();
        setupButtonHandlers();
        applyVisualEffects();

        statusLabel.setText("Ready");
        queueTable.setItems(queueList);
        bookButton.setDisable(true);
    }

    private void setupTableColumns() {
        vehicleColumn.setCellValueFactory(cellData -> cellData.getValue().vehicleProperty());
        serviceTypeColumn.setCellValueFactory(cellData -> cellData.getValue().serviceTypeProperty());
        estimatedDurationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());

        vehicleColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        serviceTypeColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        estimatedDurationColumn.setStyle("-fx-alignment: CENTER;");
    }

    private void loadWorkshops() {
        showProgress(true);
        statusLabel.setText("Loading workshops...");

        try {
            List<Workshop> workshops = workshopDAO.findApprovedWorkshops();
            workshopComboBox.getItems().setAll(workshops);
            statusLabel.setText("Loaded " + workshops.size() + " workshops");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading workshops");
            AlertUtil.showError("Load Failed", "Failed to load workshops.");
        } finally {
            showProgress(false);
        }
    }

    private void setupButtonHandlers() {
        estimateButton.setOnAction(event -> handleEstimate());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToDashboard());
        refreshButton.setOnAction(event -> {
            loadWorkshops();
            if (workshopComboBox.getValue() != null) handleEstimate();
        });
        bookButton.setOnAction(event -> handleBookAppointment());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());
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

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        estimateButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        refreshButton.setEffect(dropShadow);
        bookButton.setEffect(dropShadow);
        if (fadeButton != null) fadeButton.setEffect(dropShadow);
    }

    private void handleEstimate() {
        Workshop selectedWorkshop = workshopComboBox.getSelectionModel().getSelectedItem();

        if (selectedWorkshop == null) {
            AlertUtil.showWarning("Validation Error", "Please select a workshop.");
            return;
        }

        showOperationProgress(true);
        statusLabel.setText("Calculating wait time...");
        updateProgress(0.2);

        try {
            workshopNameLabel.setText(selectedWorkshop.getWorkshopName());
            updateProgress(0.4);

            int activeServices = getActiveServiceCount(selectedWorkshop.getId());
            int mechanicsCount = getMechanicsCount(selectedWorkshop.getId());
            int queueSize = getQueueSize(selectedWorkshop.getId());

            activeServicesLabel.setText(String.valueOf(activeServices));
            mechanicsAvailableLabel.setText(String.valueOf(Math.max(1, mechanicsCount - (activeServices / 2))));
            queueLengthLabel.setText(String.valueOf(queueSize));

            updateProgress(0.6);
            String busyLevel = getBusyLevel(activeServices, mechanicsCount);
            busyLevelLabel.setText(busyLevel);

            int estimatedHours = estimateWaitHours(activeServices, mechanicsCount, queueSize);
            estimatedWaitTimeLabel.setText(estimatedHours + " hours");

            double busyPercentage = calculateBusyPercentage(activeServices, mechanicsCount);
            busyProgressBar.setProgress(busyPercentage);
            waitTimeProgress.setProgress(Math.min(1.0, estimatedHours / 8.0));

            updateProgress(0.8);
            String busyColor = getBusyColor(busyLevel);
            busyLevelLabel.setStyle("-fx-text-fill: " + busyColor + "; -fx-font-weight: bold;");

            String recommendation = generateRecommendation(estimatedHours, busyLevel);
            recommendationLabel.setText(recommendation);

            loadQueueInfo(selectedWorkshop.getId());
            updateProgress(1.0);

            bookButton.setDisable(false);
            statusLabel.setText("Wait time calculated - " + estimatedHours + " hours");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error estimating wait time");
            AlertUtil.showError("Estimate Failed", "Failed to estimate wait time.");
        } finally {
            hideProgressAfterDelay();
        }
    }

    private int getActiveServiceCount(int workshopId) {
        try {
            List<ServiceRecord> services = serviceDAO.findByWorkshopId(workshopId);
            LocalDate today = LocalDate.now();
            return (int) services.stream()
                    .filter(s -> s.getServiceDate() != null && s.getServiceDate().equals(today))
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private int getMechanicsCount(int workshopId) {
        try {
            dao.MechanicDAO mechanicDAO = new dao.MechanicDAO();
            return mechanicDAO.countByWorkshopId(workshopId);
        } catch (Exception e) {
            return 2;
        }
    }

    private int getQueueSize(int workshopId) {
        try {
            List<ServiceRecord> services = serviceDAO.findByWorkshopId(workshopId);
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);
            return (int) services.stream()
                    .filter(s -> s.getServiceDate() != null &&
                            (s.getServiceDate().equals(today) || s.getServiceDate().equals(tomorrow)))
                    .count();
        } catch (Exception e) {
            return 0;
        }
    }

    private String getBusyLevel(int activeServices, int mechanicsCount) {
        int ratio = mechanicsCount > 0 ? activeServices / mechanicsCount : activeServices;
        if (ratio <= 1) return "LOW";
        if (ratio <= 2) return "MEDIUM";
        if (ratio <= 3) return "HIGH";
        return "VERY HIGH";
    }

    private String getBusyColor(String level) {
        switch (level) {
            case "LOW": return "#4CAF50";
            case "MEDIUM": return "#FFC107";
            case "HIGH": return "#FF9800";
            case "VERY HIGH": return "#F44336";
            default: return "#666";
        }
    }

    private double calculateBusyPercentage(int activeServices, int mechanicsCount) {
        int maxCapacity = mechanicsCount * 4;
        if (maxCapacity <= 0) return 0;
        return Math.min(1.0, (double) activeServices / maxCapacity);
    }

    private int estimateWaitHours(int activeServices, int mechanicsCount, int queueSize) {
        int effectiveMechanics = Math.max(1, mechanicsCount);
        int totalJobs = activeServices + queueSize;
        int hoursPerMechanic = (totalJobs * 2) / effectiveMechanics;

        if (hoursPerMechanic <= 1) return 1;
        if (hoursPerMechanic <= 3) return 2;
        if (hoursPerMechanic <= 5) return 3;
        if (hoursPerMechanic <= 8) return 5;
        return 8;
    }

    private String generateRecommendation(int estimatedHours, String busyLevel) {
        if ("VERY HIGH".equals(busyLevel)) {
            return "⚠️ Workshop is extremely busy. Consider visiting another workshop or book an appointment for another day.";
        } else if ("HIGH".equals(busyLevel)) {
            return "📅 Workshop is busy. Book an appointment to avoid long waiting times.";
        } else if ("MEDIUM".equals(busyLevel)) {
            return "✅ Moderate traffic. You can visit now or book an appointment online.";
        } else {
            return "🟢 Low traffic. Good time to visit!";
        }
    }

    private void loadQueueInfo(int workshopId) {
        queueList.clear();

        try {
            List<ServiceRecord> services = serviceDAO.findByWorkshopId(workshopId);
            LocalDate today = LocalDate.now();

            for (ServiceRecord service : services) {
                if (service.getServiceDate() != null &&
                        (service.getServiceDate().equals(today) || service.getServiceDate().equals(today.plusDays(1)))) {

                    QueueItem item = new QueueItem();
                    item.setVehicle(service.getRegistrationNumber());
                    item.setServiceType(service.getServiceType());

                    if (service.getServiceDate().equals(today)) {
                        item.setDuration("Today - " + estimateItemWaitTime(service));
                    } else {
                        item.setDuration("Tomorrow - " + estimateItemWaitTime(service));
                    }
                    queueList.add(item);
                }
            }

            if (queueList.isEmpty()) {
                QueueItem emptyItem = new QueueItem();
                emptyItem.setVehicle("No vehicles in queue");
                emptyItem.setServiceType("-");
                emptyItem.setDuration("-");
                queueList.add(emptyItem);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String estimateItemWaitTime(ServiceRecord service) {
        double cost = service.getCost();
        if (cost < 500) return "~1 hour";
        if (cost < 1500) return "~2-3 hours";
        if (cost < 3000) return "~4-5 hours";
        return "~6+ hours";
    }

    private void handleBookAppointment() {
        Workshop selectedWorkshop = workshopComboBox.getSelectionModel().getSelectedItem();

        if (selectedWorkshop == null) {
            AlertUtil.showWarning("No Workshop", "Please select a workshop first.");
            return;
        }

        String waitTime = estimatedWaitTimeLabel.getText();

        boolean confirmed = AlertUtil.showConfirmation("Book Appointment",
                "Book appointment at " + selectedWorkshop.getWorkshopName() + "?\n\n" +
                        "Estimated wait time: " + waitTime + "\n" +
                        "Would you like to proceed with booking?");

        if (confirmed) {
            // In a real implementation, this would save to database
            AlertUtil.showSuccess("Appointment Requested",
                    "Your service appointment has been requested.\n" +
                            selectedWorkshop.getWorkshopName() + " will contact you shortly.\n\n" +
                            "Estimated wait time: " + waitTime);
            statusLabel.setText("Appointment booked successfully");
            bookButton.setDisable(true);
        }
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
    }

    private void showOperationProgress(boolean show) {
        if (operationProgress != null) {
            operationProgress.setVisible(show);
            operationProgress.setProgress(0);
        }
    }

    private void updateProgress(double progress) {
        if (operationProgress != null) operationProgress.setProgress(progress);
    }

    private void hideProgressAfterDelay() {
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(event -> {
            if (operationProgress != null) {
                operationProgress.setVisible(false);
                operationProgress.setProgress(0);
            }
            if (loadProgress != null) loadProgress.setVisible(false);
        });
        delay.play();
    }

    public static class QueueItem {
        private final javafx.beans.property.SimpleStringProperty vehicle;
        private final javafx.beans.property.SimpleStringProperty serviceType;
        private final javafx.beans.property.SimpleStringProperty duration;

        public QueueItem() {
            this.vehicle = new javafx.beans.property.SimpleStringProperty();
            this.serviceType = new javafx.beans.property.SimpleStringProperty();
            this.duration = new javafx.beans.property.SimpleStringProperty();
        }

        public String getVehicle() { return vehicle.get(); }
        public void setVehicle(String value) { vehicle.set(value); }
        public javafx.beans.property.StringProperty vehicleProperty() { return vehicle; }

        public String getServiceType() { return serviceType.get(); }
        public void setServiceType(String value) { serviceType.set(value); }
        public javafx.beans.property.StringProperty serviceTypeProperty() { return serviceType; }

        public String getDuration() { return duration.get(); }
        public void setDuration(String value) { duration.set(value); }
        public javafx.beans.property.StringProperty durationProperty() { return duration; }
    }
}