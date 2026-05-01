package models;

import java.time.LocalDate;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ServiceSchedule extends BaseEntity {
    private int id;
    private int vehicleId;
    private String registrationNumber;
    private String serviceType;
    private LocalDate dueDate;
    private Integer dueOdometer;
    private LocalDate lastServiceDate;
    private Integer lastServiceOdometer;
    private boolean reminderSent;
    private LocalDate reminderSentDate;
    private int customerId;
    private String customerEmail;

    // JavaFX Properties for TableView binding
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty serviceTypeProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dueDateProperty = new SimpleObjectProperty<>();
    private final BooleanProperty reminderSentProperty = new SimpleBooleanProperty();

    public ServiceSchedule() {
        super();
        this.reminderSent = false;
    }

    public ServiceSchedule(int vehicleId, String serviceType, LocalDate dueDate) {
        this();
        this.vehicleId = vehicleId;
        this.serviceType = serviceType;
        this.dueDate = dueDate;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
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

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
        serviceTypeProperty.set(serviceType);
    }

    public StringProperty serviceTypeProperty() {
        return serviceTypeProperty;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        dueDateProperty.set(dueDate);
    }

    public ObjectProperty<LocalDate> dueDateProperty() {
        return dueDateProperty;
    }

    public Integer getDueOdometer() {
        return dueOdometer;
    }

    public void setDueOdometer(Integer dueOdometer) {
        this.dueOdometer = dueOdometer;
    }

    public LocalDate getLastServiceDate() {
        return lastServiceDate;
    }

    public void setLastServiceDate(LocalDate lastServiceDate) {
        this.lastServiceDate = lastServiceDate;
    }

    public Integer getLastServiceOdometer() {
        return lastServiceOdometer;
    }

    public void setLastServiceOdometer(Integer lastServiceOdometer) {
        this.lastServiceOdometer = lastServiceOdometer;
    }

    public boolean isReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
        reminderSentProperty.set(reminderSent);
    }

    public BooleanProperty reminderSentProperty() {
        return reminderSentProperty;
    }

    public LocalDate getReminderSentDate() {
        return reminderSentDate;
    }

    public void setReminderSentDate(LocalDate reminderSentDate) {
        this.reminderSentDate = reminderSentDate;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    public boolean isDueSoon() {
        if (dueDate == null) return false;
        LocalDate now = LocalDate.now();
        return dueDate.isAfter(now) && dueDate.minusDays(7).isBefore(now);
    }

    public String getStatus() {
        if (isOverdue()) return "OVERDUE";
        if (isDueSoon()) return "DUE_SOON";
        return "FUTURE";
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
        return serviceType + " due on " + dueDate;
    }
}