package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import utils.AlertUtil;
import utils.SceneManager;
import utils.SessionManager;
import dao.CustomerReviewDAO;
import dao.WorkshopDAO;
import models.CustomerReview;
import models.Workshop;

public class CustomerReviewController {

    @FXML private TableView<CustomerReview> reviewsTable;
    @FXML private TableColumn<CustomerReview, String> workshopColumn;
    @FXML private TableColumn<CustomerReview, Integer> ratingColumn;
    @FXML private TableColumn<CustomerReview, String> reviewDateColumn;
    @FXML private TableColumn<CustomerReview, String> reviewTextColumn;

    @FXML private ComboBox<Workshop> workshopComboBox;
    @FXML private TextArea reviewTextArea;
    @FXML private ToggleGroup ratingGroup;
    @FXML private RadioButton rating1Radio;
    @FXML private RadioButton rating2Radio;
    @FXML private RadioButton rating3Radio;
    @FXML private RadioButton rating4Radio;
    @FXML private RadioButton rating5Radio;

    @FXML private Button submitButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;

    private CustomerReviewDAO reviewDAO;
    private WorkshopDAO workshopDAO;
    private int customerId;

    @FXML
    public void initialize() {
        reviewDAO = new CustomerReviewDAO();
        workshopDAO = new WorkshopDAO();

        customerId = SessionManager.getInstance().getCustomerId();

        setupTableColumns();
        loadWorkshops();
        loadReviews();
        setupButtonHandlers();

        ratingGroup = new ToggleGroup();
        rating1Radio.setToggleGroup(ratingGroup);
        rating2Radio.setToggleGroup(ratingGroup);
        rating3Radio.setToggleGroup(ratingGroup);
        rating4Radio.setToggleGroup(ratingGroup);
        rating5Radio.setToggleGroup(ratingGroup);

        rating5Radio.setSelected(true);
    }

    private void setupTableColumns() {
        workshopColumn.setCellValueFactory(cellData -> cellData.getValue().workshopNameProperty());
        ratingColumn.setCellValueFactory(cellData -> cellData.getValue().ratingProperty().asObject());
        reviewDateColumn.setCellValueFactory(cellData -> cellData.getValue().reviewDateProperty().asString());
        reviewTextColumn.setCellValueFactory(cellData -> cellData.getValue().reviewTextProperty());
    }

    private void loadWorkshops() {
        try {
            java.util.List<Workshop> workshops = workshopDAO.findApprovedWorkshops();
            workshopComboBox.getItems().setAll(workshops);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadReviews() {
        try {
            java.util.List<CustomerReview> reviews;
            if (SessionManager.getInstance().isAdmin()) {
                reviews = reviewDAO.findAll();
            } else {
                reviews = reviewDAO.findByCustomerId(customerId);
            }
            reviewsTable.getItems().setAll(reviews);
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Load Failed", "Failed to load reviews.");
        }
    }

    private void setupButtonHandlers() {
        submitButton.setOnAction(event -> handleSubmit());
        refreshButton.setOnAction(event -> loadReviews());
        backButton.setOnAction(event -> SceneManager.getInstance().switchToCustomerProfileView());
    }

    private int getSelectedRating() {
        if (rating1Radio.isSelected()) return 1;
        if (rating2Radio.isSelected()) return 2;
        if (rating3Radio.isSelected()) return 3;
        if (rating4Radio.isSelected()) return 4;
        if (rating5Radio.isSelected()) return 5;
        return 5;
    }

    private void handleSubmit() {
        Workshop selectedWorkshop = workshopComboBox.getSelectionModel().getSelectedItem();

        if (selectedWorkshop == null) {
            AlertUtil.showWarning("Validation Error", "Please select a workshop.");
            return;
        }

        if (!utils.ValidationUtil.isNotEmpty(reviewTextArea.getText())) {
            AlertUtil.showWarning("Validation Error", "Please enter your review.");
            reviewTextArea.requestFocus();
            return;
        }

        int rating = getSelectedRating();

        try {
            CustomerReview review = new CustomerReview();
            review.setCustomerId(customerId);
            review.setWorkshopId(selectedWorkshop.getId());
            review.setRating(rating);
            review.setReviewText(reviewTextArea.getText().trim());

            boolean success = reviewDAO.insert(review);

            if (success) {
                AlertUtil.showSuccess("Review submitted successfully.");
                clearForm();
                loadReviews();
            } else {
                AlertUtil.showError("Submit Failed", "Failed to submit review.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Database Error", "An error occurred while submitting review.");
        }
    }

    private void clearForm() {
        workshopComboBox.getSelectionModel().clearSelection();
        reviewTextArea.clear();
        rating5Radio.setSelected(true);
    }
}