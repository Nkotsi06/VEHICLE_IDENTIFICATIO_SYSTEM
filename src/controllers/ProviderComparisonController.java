package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import utils.AlertUtil;
import utils.SceneManager;
import utils.CurrencyUtil;
import dao.InsuranceProviderDAO;
import dao.VehicleDAO;
import models.InsuranceProvider;
import models.Vehicle;

import java.util.List;

public class ProviderComparisonController {

    @FXML private TableView<InsuranceProvider> providersTable;
    @FXML private TableColumn<InsuranceProvider, String> nameColumn;
    @FXML private TableColumn<InsuranceProvider, Double> ratingColumn;
    @FXML private TableColumn<InsuranceProvider, String> phoneColumn;
    @FXML private TableColumn<InsuranceProvider, String> emailColumn;

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private Label selectedProviderLabel;
    @FXML private Label premiumEstimateLabel;
    @FXML private Label coverageSummaryLabel;

    @FXML private Button compareButton;
    @FXML private Button selectProviderButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private InsuranceProviderDAO providerDAO;
    private VehicleDAO vehicleDAO;
    private InsuranceProvider selectedProvider;

    @FXML
    public void initialize() {
        providerDAO = new InsuranceProviderDAO();
        vehicleDAO = new VehicleDAO();

        setupTableColumns();
        loadProviders();
        loadVehicles();
        setupButtonHandlers();
        setupTableSelection();
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        ratingColumn.setCellValueFactory(cellData -> cellData.getValue().ratingProperty());
        phoneColumn.setCellValueFactory(cellData -> cellData.getValue().contactPhoneProperty());
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().contactEmailProperty());
    }

    private void loadProviders() {
        try {
            List<InsuranceProvider> providers = providerDAO.findAll();
            providersTable.getItems().setAll(providers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadVehicles() {
        try {
            int customerId = utils.SessionManager.getInstance().getCustomerId();
            List<Vehicle> vehicles = vehicleDAO.findByOwnerId(customerId);
            vehicleComboBox.getItems().setAll(vehicles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupButtonHandlers() {
        compareButton.setOnAction(event -> handleCompare());
        selectProviderButton.setOnAction(event -> handleSelectProvider());
        if (refreshButton != null) {
            refreshButton.setOnAction(event -> loadProviders());
        }
        backButton.setOnAction(event -> SceneManager.getInstance().switchToInsurancePolicyView());
    }

    private void setupTableSelection() {
        providersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedProvider = newSelection;
                selectedProviderLabel.setText("Selected: " + selectedProvider.getName());

                if (vehicleComboBox.getValue() != null) {
                    calculatePremium(selectedProvider, vehicleComboBox.getValue());
                }
            }
        });
    }

    private void handleCompare() {
        if (vehicleComboBox.getValue() == null) {
            AlertUtil.showWarning("Validation Error", "Please select a vehicle.");
            return;
        }

        calculatePremiumsForAll(vehicleComboBox.getValue());
    }

    private void calculatePremiumsForAll(Vehicle vehicle) {
        int vehicleAge = java.time.LocalDate.now().getYear() - vehicle.getYear();
        double basePremium = vehicleAge <= 3 ? 2500 : (vehicleAge <= 7 ? 1800 : 1200);

        StringBuilder comparison = new StringBuilder();
        comparison.append("Premium Comparison for ").append(vehicle.getRegistrationNumber()).append(":\n\n");

        for (InsuranceProvider provider : providersTable.getItems()) {
            Double rating = provider.getRating();
            double multiplier = (rating != null && rating > 0) ? (5.0 / rating) : 1.0;
            double premium = basePremium * multiplier;
            comparison.append(provider.getName()).append(": ").append(CurrencyUtil.format(premium)).append("\n");
        }

        AlertUtil.showInfo("Premium Comparison", comparison.toString());
    }

    private void calculatePremium(InsuranceProvider provider, Vehicle vehicle) {
        int vehicleAge = java.time.LocalDate.now().getYear() - vehicle.getYear();
        double basePremium = vehicleAge <= 3 ? 2500 : (vehicleAge <= 7 ? 1800 : 1200);
        Double rating = provider.getRating();
        double multiplier = (rating != null && rating > 0) ? (5.0 / rating) : 1.0;
        double premium = basePremium * multiplier;

        premiumEstimateLabel.setText(CurrencyUtil.format(premium));
        coverageSummaryLabel.setText(provider.getCoverageDetails() != null ?
                provider.getCoverageDetails().substring(0, Math.min(100, provider.getCoverageDetails().length())) + "..." :
                "Standard coverage available");
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
                "Proceed with " + selectedProvider.getName() + " for vehicle " + vehicleComboBox.getValue().getRegistrationNumber() + "?");

        if (confirmed) {
            AlertUtil.showSuccess("Provider selected. You can now create a policy.");
            SceneManager.getInstance().switchToInsurancePolicyView();
        }
    }
}