package models;

import java.time.LocalDateTime;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CustomerQuery extends BaseEntity {
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

    public CustomerQuery() {
        super();
        this.status = "PENDING";
    }

    public CustomerQuery(int customerId, int vehicleId, String queryText) {
        this();
        this.customerId = customerId;
        this.vehicleId = vehicleId;
        this.queryText = queryText;
        this.queryDate = LocalDateTime.now();

        // Update properties
        customerIdProperty.set(customerId);
        vehicleIdProperty.set(vehicleId);
        queryTextProperty.set(queryText);
        queryDateProperty.set(queryDate);
        statusProperty.set("PENDING");
    }

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

    public boolean isPending() {
        return "PENDING".equals(status);
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
        return queryText.substring(0, Math.min(50, queryText.length())) + "... - " + status;
    }
}