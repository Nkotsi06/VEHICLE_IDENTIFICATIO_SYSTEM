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
 * Officer model representing police officers in the system.
 * Contains personal information, badge details, and rank information.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class Officer extends BaseEntity {

    // Core fields
    private int id;
    private int userId;
    private String username;
    private String fullName;
    private String email;
    private String badgeNumber;
    private String rank;
    private String station;
    private String division;
    private LocalDate joinedDate;
    private String specialization;
    private boolean isActive;
    private String phone;
    private String address;

    // Rank constants
    public static final String RANK_CHIEF = "CHIEF";
    public static final String RANK_INSPECTOR = "INSPECTOR";
    public static final String RANK_SERGEANT = "SERGEANT";
    public static final String RANK_CORPORAL = "CORPORAL";
    public static final String RANK_CONSTABLE = "CONSTABLE";

    // Division constants
    public static final String DIVISION_TRAFFIC = "TRAFFIC";
    public static final String DIVISION_CRIMINAL_INVESTIGATION = "CRIMINAL_INVESTIGATION";
    public static final String DIVISION_PATROL = "PATROL";
    public static final String DIVISION_ADMIN = "ADMIN";

    // JavaFX Properties
    private final IntegerProperty userIdProperty = new SimpleIntegerProperty();
    private final StringProperty usernameProperty = new SimpleStringProperty();
    private final StringProperty fullNameProperty = new SimpleStringProperty();
    private final StringProperty emailProperty = new SimpleStringProperty();
    private final StringProperty badgeNumberProperty = new SimpleStringProperty();
    private final StringProperty rankProperty = new SimpleStringProperty();
    private final StringProperty stationProperty = new SimpleStringProperty();
    private final StringProperty divisionProperty = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> joinedDateProperty = new SimpleObjectProperty<>();
    private final StringProperty specializationProperty = new SimpleStringProperty();
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final StringProperty phoneProperty = new SimpleStringProperty();
    private final StringProperty addressProperty = new SimpleStringProperty();
    private final StringProperty rankDisplayProperty = new SimpleStringProperty();
    private final StringProperty rankPriorityProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with ACTIVE status and current date.
     */
    public Officer() {
        super();
        this.isActive = true;
        this.joinedDate = LocalDate.now();

        activeProperty.set(true);
        joinedDateProperty.set(joinedDate);
        updateRankDisplay();

        rankProperty.addListener((obs, oldVal, newVal) -> updateRankDisplay());
    }

    /**
     * Constructor for creating a new officer with basic info.
     *
     * @param badgeNumber the officer's badge number
     * @param rank        the officer's rank
     * @param station     the assigned station
     * @param division    the division
     */
    public Officer(String badgeNumber, String rank, String station, String division) {
        this();
        this.badgeNumber = badgeNumber;
        this.rank = rank;
        this.station = station;
        this.division = division;
        this.joinedDate = LocalDate.now();

        badgeNumberProperty.set(badgeNumber);
        rankProperty.set(rank);
        stationProperty.set(station);
        divisionProperty.set(division);
        joinedDateProperty.set(joinedDate);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateRankDisplay() {
        switch (rank) {
            case RANK_CHIEF:
                rankDisplayProperty.set("Chief of Police");
                rankPriorityProperty.set("1");
                break;
            case RANK_INSPECTOR:
                rankDisplayProperty.set("Inspector");
                rankPriorityProperty.set("2");
                break;
            case RANK_SERGEANT:
                rankDisplayProperty.set("Sergeant");
                rankPriorityProperty.set("3");
                break;
            case RANK_CORPORAL:
                rankDisplayProperty.set("Corporal");
                rankPriorityProperty.set("4");
                break;
            case RANK_CONSTABLE:
                rankDisplayProperty.set("Constable");
                rankPriorityProperty.set("5");
                break;
            default:
                rankDisplayProperty.set(rank != null ? rank : "Unknown");
                rankPriorityProperty.set("9");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
    // ============================================

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
        userIdProperty.set(userId);
    }

    public IntegerProperty userIdProperty() {
        return userIdProperty;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        usernameProperty.set(username);
    }

    public StringProperty usernameProperty() {
        return usernameProperty;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        fullNameProperty.set(fullName);
    }

    public StringProperty fullNameProperty() {
        return fullNameProperty;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        emailProperty.set(email);
    }

    public StringProperty emailProperty() {
        return emailProperty;
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(String badgeNumber) {
        this.badgeNumber = badgeNumber;
        badgeNumberProperty.set(badgeNumber);
    }

    public StringProperty badgeNumberProperty() {
        return badgeNumberProperty;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
        rankProperty.set(rank);
    }

    public StringProperty rankProperty() {
        return rankProperty;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
        stationProperty.set(station);
    }

    public StringProperty stationProperty() {
        return stationProperty;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
        divisionProperty.set(division);
    }

    public StringProperty divisionProperty() {
        return divisionProperty;
    }

    public LocalDate getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDate joinedDate) {
        this.joinedDate = joinedDate;
        joinedDateProperty.set(joinedDate);
    }

    public ObjectProperty<LocalDate> joinedDateProperty() {
        return joinedDateProperty;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        activeProperty.set(active);
    }

    public BooleanProperty activeProperty() {
        return activeProperty;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        addressProperty.set(address);
    }

    public StringProperty addressProperty() {
        return addressProperty;
    }

    public String getRankDisplay() {
        return rankDisplayProperty.get();
    }

    public StringProperty rankDisplayProperty() {
        return rankDisplayProperty;
    }

    public String getRankPriority() {
        return rankPriorityProperty.get();
    }

    public StringProperty rankPriorityProperty() {
        return rankPriorityProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public String getDivisionDisplay() {
        switch (division) {
            case DIVISION_TRAFFIC: return "Traffic Division";
            case DIVISION_CRIMINAL_INVESTIGATION: return "Criminal Investigation";
            case DIVISION_PATROL: return "Patrol Division";
            case DIVISION_ADMIN: return "Administration";
            default: return division != null ? division : "Unknown";
        }
    }

    public String getFormattedJoinedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return joinedDate != null ? joinedDate.format(formatter) : "";
    }

    public String getYearsOfService() {
        if (joinedDate == null) return "N/A";
        int years = LocalDate.now().getYear() - joinedDate.getYear();
        return years + " year" + (years != 1 ? "s" : "");
    }

    public String getDisplayName() {
        return rankDisplayProperty.get() + " " + fullName + " (" + badgeNumber + ")";
    }

    public boolean isTrafficOfficer() {
        return DIVISION_TRAFFIC.equals(division);
    }

    public boolean isInvestigator() {
        return DIVISION_CRIMINAL_INVESTIGATION.equals(division);
    }

    public void activate() {
        setActive(true);
    }

    public void deactivate() {
        setActive(false);
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
        return rankDisplayProperty.get() + " " + fullName + " - " + badgeNumber;
    }

    /**
     * Creates a copy of this officer.
     *
     * @return a new Officer instance
     */
    public Officer copy() {
        Officer copy = new Officer();
        copy.setId(this.id);
        copy.setUserId(this.userId);
        copy.setUsername(this.username);
        copy.setFullName(this.fullName);
        copy.setEmail(this.email);
        copy.setBadgeNumber(this.badgeNumber);
        copy.setRank(this.rank);
        copy.setStation(this.station);
        copy.setDivision(this.division);
        copy.setJoinedDate(this.joinedDate);
        copy.setSpecialization(this.specialization);
        copy.setActive(this.isActive);
        copy.setPhone(this.phone);
        copy.setAddress(this.address);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}