package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * CustomerReview model representing customer reviews and ratings for workshops.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class CustomerReview extends BaseEntity {

    // Core fields
    private int id;
    private int customerId;
    private String customerName;
    private int workshopId;
    private String workshopName;
    private int rating;
    private String reviewText;
    private LocalDateTime reviewDate;

    // Rating constants
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
    public static final int DEFAULT_RATING = 5;

    // JavaFX Properties for TableView binding
    private final StringProperty workshopNameProperty = new SimpleStringProperty();
    private final IntegerProperty ratingProperty = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDateTime> reviewDateProperty = new SimpleObjectProperty<>();
    private final StringProperty reviewTextProperty = new SimpleStringProperty();
    private final StringProperty customerNameProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public CustomerReview() {
        super();
        this.reviewDate = LocalDateTime.now();
        this.rating = DEFAULT_RATING;

        ratingProperty.set(DEFAULT_RATING);
        reviewDateProperty.set(reviewDate);
    }

    /**
     * Constructor for creating a new review.
     *
     * @param customerId the customer ID
     * @param workshopId the workshop ID
     * @param rating     the rating (1-5)
     * @param reviewText the review text
     */
    public CustomerReview(int customerId, int workshopId, int rating, String reviewText) {
        this();
        this.customerId = customerId;
        this.workshopId = workshopId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.reviewDate = LocalDateTime.now();

        ratingProperty.set(rating);
        reviewTextProperty.set(reviewText);
        reviewDateProperty.set(reviewDate);
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
        customerNameProperty.set(customerName);
    }

    public StringProperty customerNameProperty() {
        return customerNameProperty;
    }

    public int getWorkshopId() {
        return workshopId;
    }

    public void setWorkshopId(int workshopId) {
        this.workshopId = workshopId;
    }

    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
        workshopNameProperty.set(workshopName);
    }

    public StringProperty workshopNameProperty() {
        return workshopNameProperty;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        // Ensure rating is within valid range
        int validRating = Math.max(MIN_RATING, Math.min(MAX_RATING, rating));
        this.rating = validRating;
        ratingProperty.set(validRating);
    }

    public IntegerProperty ratingProperty() {
        return ratingProperty;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
        reviewTextProperty.set(reviewText);
    }

    public StringProperty reviewTextProperty() {
        return reviewTextProperty;
    }

    public LocalDateTime getReviewDate() {
        return reviewDate;
    }

    public void setReviewDate(LocalDateTime reviewDate) {
        this.reviewDate = reviewDate;
        reviewDateProperty.set(reviewDate);
    }

    public ObjectProperty<LocalDateTime> reviewDateProperty() {
        return reviewDateProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Gets star characters for display (using unicode stars).
     *
     * @return string of stars (★ for rated, ☆ for unrated)
     */
    public String getRatingStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        for (int i = rating; i < MAX_RATING; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }

    /**
     * Gets HTML star display.
     *
     * @return HTML string with colored stars
     */
    public String getRatingStarsHtml() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("<span style='color: #FFD700;'>★</span>");
        }
        for (int i = rating; i < MAX_RATING; i++) {
            stars.append("<span style='color: #CCCCCC;'>☆</span>");
        }
        return stars.toString();
    }

    /**
     * Gets the rating percentage (0-100).
     *
     * @return rating percentage
     */
    public int getRatingPercentage() {
        return (rating * 100) / MAX_RATING;
    }

    /**
     * Gets the formatted review date.
     *
     * @return formatted date string
     */
    public String getFormattedReviewDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return reviewDate != null ? reviewDate.format(formatter) : "";
    }

    /**
     * Gets the review preview (first 100 characters).
     *
     * @return preview text
     */
    public String getReviewPreview() {
        if (reviewText == null) return "";
        if (reviewText.length() <= 100) return reviewText;
        return reviewText.substring(0, 100) + "...";
    }

    /**
     * Gets the rating label (e.g., "Excellent", "Good", etc.).
     *
     * @return rating label
     */
    public String getRatingLabel() {
        switch (rating) {
            case 5: return "Excellent";
            case 4: return "Good";
            case 3: return "Average";
            case 2: return "Poor";
            case 1: return "Terrible";
            default: return "Unknown";
        }
    }

    /**
     * Gets the rating color class.
     *
     * @return CSS color class
     */
    public String getRatingColorClass() {
        if (rating >= 4) return "success";
        if (rating >= 3) return "warning";
        return "danger";
    }

    /**
     * Validates that the review has all required fields.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return rating >= MIN_RATING && rating <= MAX_RATING &&
                reviewText != null && !reviewText.trim().isEmpty();
    }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return rating + " stars - " + getReviewPreview();
    }

    /**
     * Creates a copy of this review.
     *
     * @return a new CustomerReview instance
     */
    public CustomerReview copy() {
        CustomerReview copy = new CustomerReview();
        copy.setId(this.id);
        copy.setCustomerId(this.customerId);
        copy.setCustomerName(this.customerName);
        copy.setWorkshopId(this.workshopId);
        copy.setWorkshopName(this.workshopName);
        copy.setRating(this.rating);
        copy.setReviewText(this.reviewText);
        copy.setReviewDate(this.reviewDate);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}