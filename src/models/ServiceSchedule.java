package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ServiceSchedule model representing scheduled vehicle service reminders.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class ServiceSchedule extends BaseEntity {

    // Core fields
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

    // Status constants
    public static final String STATUS_OVERDUE = "OVERDUE";
    public static final String STATUS_DUE_SOON = "DUE_SOON";
    public static final String STATUS_FUTURE = "FUTURE";
    public static final String STATUS_COMPLETED = "COMPLETED";

    // Service type constants
    public static final String SERVICE_OIL = "OIL_CHANGE";
    public static final String SERVICE_TUNE_UP = "TUNE_UP";
    public static final String SERVICE_BRAKE = "BRAKE_SERVICE";
    public static final String SERVICE_TIRE = "TIRE_ROTATION";
    public static final String SERVICE_INSPECTION = "GENERAL_INSPECTION";

    // JavaFX Properties for TableView binding
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();
    private final StringProperty serviceTypeProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dueDateProperty = new SimpleObjectProperty<>();
    private final BooleanProperty reminderSentProperty = new SimpleBooleanProperty();
    private final IntegerProperty dueOdometerProperty = new SimpleIntegerProperty();
    private final StringProperty statusProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();
    private final StringProperty formattedDueDateProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public ServiceSchedule() {
        super();
        this.reminderSent = false;
        reminderSentProperty.set(false);

        dueDateProperty.addListener((obs, oldVal, newVal) -> updateStatusAndFormat());
        updateStatusAndFormat();
    }

    /**
     * Constructor for creating a new service schedule.
     *
     * @param vehicleId   the vehicle ID
     * @param serviceType the service type
     * @param dueDate     the due date
     */
    public ServiceSchedule(int vehicleId, String serviceType, LocalDate dueDate) {
        this();
        this.vehicleId = vehicleId;
        this.serviceType = serviceType;
        this.dueDate = dueDate;

        dueDateProperty.set(dueDate);
        serviceTypeProperty.set(serviceType);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateStatusAndFormat() {
        updateFormattedDueDate();
        String status = getStatus();
        statusProperty.set(status);

        switch (status) {
            case STATUS_OVERDUE:
                statusColorProperty.set("#F44336");
                break;
            case STATUS_DUE_SOON:
                statusColorProperty.set("#FF9800");
                break;
            case STATUS_FUTURE:
                statusColorProperty.set("#4CAF50");
                break;
            case STATUS_COMPLETED:
                statusColorProperty.set("#9E9E9E");
                break;
            default:
                statusColorProperty.set("#9E9E9E");
        }
    }

    private void updateFormattedDueDate() {
        if (dueDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formattedDueDateProperty.set(dueDate.format(formatter));
        } else {
            formattedDueDateProperty.set("");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

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
        if (dueOdometer != null) dueOdometerProperty.set(dueOdometer);
    }

    public IntegerProperty dueOdometerProperty() {
        return dueOdometerProperty;
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

    public String getFormattedDueDate() {
        return formattedDueDateProperty.get();
    }

    public StringProperty formattedDueDateProperty() {
        return formattedDueDateProperty;
    }

    public String getStatus() {
        if (dueDate == null) return STATUS_FUTURE;
        LocalDate now = LocalDate.now();
        if (dueDate.isBefore(now)) return STATUS_OVERDUE;
        if (dueDate.minusDays(7).isBefore(now)) return STATUS_DUE_SOON;
        return STATUS_FUTURE;
    }

    public StringProperty statusProperty() {
        return statusProperty;
    }

    public String getStatusColor() {
        return statusColorProperty.get();
    }

    public StringProperty statusColorProperty() {
        return statusColorProperty;
    }

    public boolean isOverdue() {
        return dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    public boolean isDueSoon() {
        if (dueDate == null) return false;
        LocalDate now = LocalDate.now();
        return dueDate.isAfter(now) && dueDate.minusDays(7).isBefore(now);
    }

    public String getServiceTypeDisplay() {
        switch (serviceType) {
            case SERVICE_OIL: return "Oil Change";
            case SERVICE_TUNE_UP: return "Tune Up";
            case SERVICE_BRAKE: return "Brake Service";
            case SERVICE_TIRE: return "Tire Rotation";
            case SERVICE_INSPECTION: return "General Inspection";
            default: return serviceType != null ? serviceType.replace("_", " ") : "Unknown";
        }
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
        return getServiceTypeDisplay() + " due on " + getFormattedDueDate();
    }

    /**
     * Creates a copy of this service schedule.
     *
     * @return a new ServiceSchedule instance
     */
    public ServiceSchedule copy() {
        ServiceSchedule copy = new ServiceSchedule();
        copy.setId(this.id);
        copy.setVehicleId(this.vehicleId);
        copy.setRegistrationNumber(this.registrationNumber);
        copy.setServiceType(this.serviceType);
        copy.setDueDate(this.dueDate);
        copy.setDueOdometer(this.dueOdometer);
        copy.setLastServiceDate(this.lastServiceDate);
        copy.setLastServiceOdometer(this.lastServiceOdometer);
        copy.setReminderSent(this.reminderSent);
        copy.setReminderSentDate(this.reminderSentDate);
        copy.setCustomerId(this.customerId);
        copy.setCustomerEmail(this.customerEmail);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}