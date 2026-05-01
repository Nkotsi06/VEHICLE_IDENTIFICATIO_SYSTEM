package models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Mechanic extends BaseEntity {
    private int id;
    private int workshopId;
    private String workshopName;
    private String name;
    private String specialization;
    private String phone;

    // JavaFX Properties for TableView binding
    private final StringProperty nameProperty = new SimpleStringProperty();
    private final StringProperty specializationProperty = new SimpleStringProperty();
    private final StringProperty phoneProperty = new SimpleStringProperty();

    public Mechanic() {
        super();
    }

    public Mechanic(int workshopId, String name, String specialization, String phone) {
        this();
        this.workshopId = workshopId;
        this.name = name;
        this.specialization = specialization;
        this.phone = phone;

        // Update properties
        nameProperty.set(name);
        specializationProperty.set(specialization);
        phoneProperty.set(phone);
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
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        nameProperty.set(name);
    }

    public StringProperty nameProperty() {
        return nameProperty;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
        specializationProperty.set(specialization);
    }

    public StringProperty specializationProperty() {
        return specializationProperty;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        phoneProperty.set(phone);
    }

    public StringProperty phoneProperty() {
        return phoneProperty;
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
        return name + " - " + specialization;
    }
}