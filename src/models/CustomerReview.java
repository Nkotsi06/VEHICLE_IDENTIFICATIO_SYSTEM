package models;

import java.time.LocalDateTime;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CustomerReview extends BaseEntity {
    private int id;
    private int customerId;
    private String customerName;
    private int workshopId;
    private String workshopName;
    private int rating;
    private String reviewText;
    private LocalDateTime reviewDate;

    // JavaFX Properties for TableView binding
    private final StringProperty workshopNameProperty = new SimpleStringProperty();
    private final IntegerProperty ratingProperty = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDateTime> reviewDateProperty = new SimpleObjectProperty<>();
    private final StringProperty reviewTextProperty = new SimpleStringProperty();

    public CustomerReview() {
        super();
    }

    public CustomerReview(int customerId, int workshopId, int rating, String reviewText) {
        this();
        this.customerId = customerId;
        this.workshopId = workshopId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.reviewDate = LocalDateTime.now();

        // Update properties
        ratingProperty.set(rating);
        reviewTextProperty.set(reviewText);
        reviewDateProperty.set(reviewDate);
    }

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
        this.rating = rating;
        ratingProperty.set(rating);
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

    public String getRatingStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("*");
        }
        for (int i = rating; i < 5; i++) {
            stars.append(".");
        }
        return stars.toString();
    }

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
        return rating + " stars - " + reviewText.substring(0, Math.min(50, reviewText.length()));
    }
}