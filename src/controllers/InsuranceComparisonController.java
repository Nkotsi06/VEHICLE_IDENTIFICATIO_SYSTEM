package controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import utils.ValidationUtil;
import utils.CurrencyUtil;
import dao.InsuranceProviderDAO;
import dao.VehicleDAO;
import models.InsuranceProvider;
import models.Vehicle;

import java.util.List;

public class InsuranceComparisonController {

    @FXML private TableView<InsuranceProvider> providersTable;
    @FXML private TableColumn<InsuranceProvider, String> nameColumn;
    @FXML private TableColumn<InsuranceProvider, Number> ratingColumn;
    @FXML private TableColumn<InsuranceProvider, String> phoneColumn;
    @FXML private TableColumn<InsuranceProvider, String> emailColumn;
    @FXML private TableColumn<InsuranceProvider, Number> estimatedPremiumColumn;

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private Label selectedProviderLabel;
    @FXML private Label premiumEstimateLabel;
    @FXML private Label coverageSummaryLabel;
    @FXML private Label statusLabel;

    @FXML private Button compareButton;
    @FXML private Button selectProviderButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button fadeButton;

    @FXML private ProgressIndicator loadProgress;

    private InsuranceProviderDAO providerDAO;
    private VehicleDAO vehicleDAO;
    private InsuranceProvider selectedProvider;
    private double calculatedPremium = 0;

    @FXML
    public void initialize() {
        providerDAO = new InsuranceProviderDAO();
        vehicleDAO = new VehicleDAO();

        setupTableColumns();
        loadVehicles();
        setupButtonHandlers();
        setupTableSelection();
        applyVisualEffects();
        statusLabel.setText("Ready");
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        ratingColumn.setCellValueFactory(cellData -> {
            Double rating = cellData.getValue().getRating();
            return new javafx.beans.property.SimpleDoubleProperty(rating != null ? rating : 0);
        });
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().contactPhoneProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().contactEmailProperty());
        estimatedPremiumColumn.setCellValueFactory(cellData -> {
            // Use a temporary value since InsuranceProvider doesn't have premium property
            // This will be calculated and stored in a map or we'll use a wrapper
            return new javafx.beans.property.SimpleDoubleProperty(0);
        });

        nameColumn.setStyle("-fx-alignment: CENTER-LEFT;");
        ratingColumn.setStyle("-fx-alignment: CENTER;");
        phoneColumn.setStyle("-fx-alignment: CENTER;");
        emailColumn.setStyle("-fx-alignment: CENTER;");
        estimatedPremiumColumn.setStyle("-fx-alignment: CENTER-RIGHT;");
    }

    private void applyVisualEffects() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        compareButton.setEffect(dropShadow);
        selectProviderButton.setEffect(dropShadow);
        if (refreshButton != null) refreshButton.setEffect(dropShadow);
        backButton.setEffect(dropShadow);
        if (fadeButton != null) fadeButton.setEffect(dropShadow);
    }

    private void loadVehicles() {
        showProgress(true);
        try {
            int customerId = SessionManager.getInstance().getCustomerId();
            List<Vehicle> vehicles = vehicleDAO.findByOwnerId(customerId);
            vehicleComboBox.getItems().setAll(vehicles);
            statusLabel.setText("Loaded " + vehicles.size() + " vehicles");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading vehicles");
            AlertUtil.showError("Load Failed", "Failed to load vehicles.");
        } finally {
            showProgress(false);
        }
    }

    private void loadProviders() {
        showProgress(true);
        statusLabel.setText("Loading providers...");

        try {
            List<InsuranceProvider> providers = providerDAO.findActiveProviders();
            providersTable.getItems().setAll(providers);
            statusLabel.setText("Loaded " + providers.size() + " providers");
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading providers");
            AlertUtil.showError("Load Failed", "Failed to load providers.");
        } finally {
            showProgress(false);
        }
    }

    private void setupButtonHandlers() {
        compareButton.setOnAction(event -> handleCompare());
        selectProviderButton.setOnAction(event -> handleSelectProvider());
        if (refreshButton != null) refreshButton.setOnAction(event -> {
            if (vehicleComboBox.getValue() != null) handleCompare();
        });
        backButton.setOnAction(event -> SceneManager.getInstance().switchToInsurancePolicyView());
        if (fadeButton != null) fadeButton.setOnAction(event -> showFadeAnimation());
    }

    private void showFadeAnimation() {
        if (fadeButton != null) {
            javafx.animation.FadeTransition fadeTransition = new javafx.animation.FadeTransition(Duration.seconds(1.5), fadeButton);
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

    private void setupTableSelection() {
        providersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedProvider = newSelection;
                selectedProviderLabel.setText("Selected: " + selectedProvider.getName());

                if (vehicleComboBox.getValue() != null) {
                    calculatePremium(selectedProvider, vehicleComboBox.getValue());
                }
                selectProviderButton.setDisable(false);
            }
        });
    }

    private void handleCompare() {
        Vehicle selectedVehicle = vehicleComboBox.getSelectionModel().getSelectedItem();

        if (selectedVehicle == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        loadProviders();
        calculatePremiumsForAll(selectedVehicle);
    }

    private void calculatePremiumsForAll(Vehicle vehicle) {
        int vehicleAge = java.time.LocalDate.now().getYear() - vehicle.getYear();
        double basePremium = calculateBasePremium(vehicleAge, vehicle.getMake());

        for (InsuranceProvider provider : providersTable.getItems()) {
            double premium = calculateProviderPremium(provider, basePremium);
            // Since InsuranceProvider doesn't have a premium property, we'll store this in a separate map
            // For now, just calculate and display
        }
        providersTable.refresh();

        statusLabel.setText("Premiums calculated for " + providersTable.getItems().size() + " providers");
    }

    private double calculateBasePremium(int vehicleAge, String make) {
        if (vehicleAge <= 3) return 2500.00;
        if (vehicleAge <= 7) return 1800.00;
        if (vehicleAge <= 10) return 1200.00;
        return 800.00;
    }

    private double calculateProviderPremium(InsuranceProvider provider, double basePremium) {
        double multiplier = 1.0;

        if (provider.getRating() != null) {
            multiplier = 5.0 / Math.max(1.0, provider.getRating());
        }

        double adjustedPremium = basePremium * multiplier;

        if ("INACTIVE".equals(provider.getStatus())) {
            adjustedPremium *= 1.5;
        }

        return Math.round(adjustedPremium * 100) / 100.0;
    }

    private void calculatePremium(InsuranceProvider provider, Vehicle vehicle) {
        int vehicleAge = java.time.LocalDate.now().getYear() - vehicle.getYear();
        double basePremium = calculateBasePremium(vehicleAge, vehicle.getMake());
        calculatedPremium = calculateProviderPremium(provider, basePremium);

        premiumEstimateLabel.setText(CurrencyUtil.format(calculatedPremium));

        String coverage = provider.getCoverageDetails();
        if (coverage != null && coverage.length() > 100) {
            coverage = coverage.substring(0, 100) + "...";
        }
        coverageSummaryLabel.setText(coverage != null ? coverage : "Standard coverage available");
    }

    private void handleSelectProvider() {
        if (selectedProvider == null) {
            AlertUtil.showWarning("No Selection", "Please select a provider.");
            return;
        }

        if (vehicleComboBox.getValue() == null) {
            AlertUtil.showWarning("No Vehicle", "Please select a vehicle.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation("Select Provider",
                "Proceed with " + selectedProvider.getName() + " for vehicle " +
                        vehicleComboBox.getValue().getRegistrationNumber() + "?\n\n" +
                        "Estimated Premium: " + CurrencyUtil.format(calculatedPremium));

        if (confirmed) {
            AlertUtil.showSuccess("Provider selected. You can now create a policy.");
            SceneManager.getInstance().switchToInsurancePolicyView();
        }
    }

    private void showProgress(boolean show) {
        if (loadProgress != null) loadProgress.setVisible(show);
    }
}