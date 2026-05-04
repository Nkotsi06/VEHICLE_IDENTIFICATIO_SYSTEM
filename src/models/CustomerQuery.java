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
 * CustomerQuery model representing customer questions/quiries about their vehicles.
 * Tracks questions from customers and responses from support staff.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class CustomerQuery extends BaseEntity {

    // Core fields
    private int id;
    private int customerId;
    private String customerName;
    private int vehicleId;
    private String registrationNumber;
    private LocalDateTime queryDate;
    private String queryText;
    private String responseText;
    private LocalDateTime responseDate;
    private String status;

    // Status constants
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ANSWERED = "ANSWERED";
    public static final String STATUS_CLOSED = "CLOSED";
    public static final String STATUS_ESCALATED = "ESCALATED";

    // JavaFX Properties for TableView binding
    private final IntegerProperty customerIdProperty = new SimpleIntegerProperty();
    private final StringProperty customerNameProperty = new SimpleStringProperty();
    private final IntegerProperty vehicleIdProperty = new SimpleIntegerProperty();
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> queryDateProperty = new SimpleObjectProperty<>();
    private final StringProperty queryTextProperty = new SimpleStringProperty();
    private final StringProperty responseTextProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> responseDateProperty = new SimpleObjectProperty<>();
    private final StringProperty statusProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with PENDING status.
     */
    public CustomerQuery() {
        super();
        this.status = STATUS_PENDING;
        this.queryDate = LocalDateTime.now();

        statusProperty.set(STATUS_PENDING);
        queryDateProperty.set(queryDate);
    }

    /**
     * Constructor for creating a new customer query.
     *
     * @param customerId the customer ID
     * @param vehicleId  the vehicle ID
     * @param queryText  the query text
     */
    public CustomerQuery(int customerId, int vehicleId, String queryText) {
        this();
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.queryText = queryText;
        this.queryDate = LocalDateTime.now();

        customerIdProperty.set(customerId);
        vehicleIdProperty.set(vehicleId);
        queryTextProperty.set(queryText);
        queryDateProperty.set(queryDate);
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
        customerIdProperty.set(customerId);
    }

    public IntegerProperty customerIdProperty() {
        return customerIdProperty;
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

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
        vehicleIdProperty.set(vehicleId);
    }

    public IntegerProperty vehicleIdProperty() {
        return vehicleIdProperty;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        registrationNumberProperty.set(registrationNumber);
    }

    public StringProperty registrationNumberProperty() {
        return registrationNumberProperty;
    }

    public LocalDateTime getQueryDate() {
        return queryDate;
    }

    public void setQueryDate(LocalDateTime queryDate) {
        this.queryDate = queryDate;
        queryDateProperty.set(queryDate);
    }

    public ObjectProperty<LocalDateTime> queryDateProperty() {
        return queryDateProperty;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
        queryTextProperty.set(queryText);
    }

    public StringProperty queryTextProperty() {
        return queryTextProperty;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
        responseTextProperty.set(responseText);

        // If response is added, mark as answered
        if (responseText != null && !responseText.trim().isEmpty() && STATUS_PENDING.equals(status)) {
            this.status = STATUS_ANSWERED;
            this.responseDate = LocalDateTime.now();
            statusProperty.set(STATUS_ANSWERED);
            responseDateProperty.set(this.responseDate);
        }
    }

    public StringProperty responseTextProperty() {
        return responseTextProperty;
    }

    public LocalDateTime getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(LocalDateTime responseDate) {
        this.responseDate = responseDate;
        responseDateProperty.set(responseDate);
    }

    public ObjectProperty<LocalDateTime> responseDateProperty() {
        return responseDateProperty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        statusProperty.set(status);
    }

    public StringProperty statusProperty() {
        return statusProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    /**
     * Checks if the query is pending.
     *
     * @return true if status is PENDING
     */
    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    /**
     * Checks if the query has been answered.
     *
     * @return true if status is ANSWERED
     */
    public boolean isAnswered() {
        return STATUS_ANSWERED.equals(status);
    }

    /**
     * Checks if the query is closed.
     *
     * @return true if status is CLOSED
     */
    public boolean isClosed() {
        return STATUS_CLOSED.equals(status);
    }

    /**
     * Gets the status display name.
     *
     * @return human-readable status
     */
    public String getStatusDisplay() {
        switch (status) {
            case STATUS_PENDING: return "Pending";
            case STATUS_ANSWERED: return "Answered";
            case STATUS_CLOSED: return "Closed";
            case STATUS_ESCALATED: return "Escalated";
            default: return status;
        }
    }

    /**
     * Gets the CSS color for the status.
     *
     * @return hex color code
     */
    public String getStatusColor() {
        switch (status) {
            case STATUS_PENDING: return "#FF9800";
            case STATUS_ANSWERED: return "#4CAF50";
            case STATUS_CLOSED: return "#9E9E9E";
            case STATUS_ESCALATED: return "#F44336";
            default: return "#9E9E9E";
        }
    }

    /**
     * Gets the formatted query date.
     *
     * @return formatted date-time string
     */
    public String getFormattedQueryDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return queryDate != null ? queryDate.format(formatter) : "";
    }

    /**
     * Gets the formatted response date.
     *
     * @return formatted date-time string
     */
    public String getFormattedResponseDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return responseDate != null ? responseDate.format(formatter) : "";
    }

    /**
     * Gets the response time in hours.
     *
     * @return response time in hours, or -1 if not answered
     */
    public long getResponseTimeHours() {
        if (queryDate == null || responseDate == null) return -1;
        return java.time.Duration.between(queryDate, responseDate).toHours();
    }

    /**
     * Gets the query preview (first 100 characters).
     *
     * @return preview text
     */
    public String getQueryPreview() {
        if (queryText == null) return "";
        if (queryText.length() <= 100) return queryText;
        return queryText.substring(0, 100) + "...";
    }

    /**
     * Answers the query with a response.
     *
     * @param response the response text
     */
    public void answer(String response) {
        this.responseText = response;
        this.status = STATUS_ANSWERED;
        this.responseDate = LocalDateTime.now();
        responseTextProperty.set(response);
        statusProperty.set(STATUS_ANSWERED);
        responseDateProperty.set(this.responseDate);
    }

    /**
     * Closes the query.
     */
    public void close() {
        this.status = STATUS_CLOSED;
        statusProperty.set(STATUS_CLOSED);
    }

    /**
     * Escalates the query to admin.
     */
    public void escalate() {
        this.status = STATUS_ESCALATED;
        statusProperty.set(STATUS_ESCALATED);
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
        return getQueryPreview() + " - " + getStatusDisplay();
    }

    /**
     * Creates a copy of this customer query.
     *
     * @return a new CustomerQuery instance
     */
    public CustomerQuery copy() {
        CustomerQuery copy = new CustomerQuery();
        copy.setId(this.id);
        copy.setCustomerId(this.customerId);
        copy.setCustomerName(this.customerName);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setQueryDate(this.queryDate);
        copy.setQueryText(this.queryText);
        copy.setResponseText(this.responseText);
        copy.setResponseDate(this.responseDate);
        copy.setStatus(this.status);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}