package models;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * PoliceOfficer model representing police officers in the system.
 * Extends PoliceOfficer with additional fields and JavaFX properties.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class PoliceOfficer extends BaseEntity {

    // Rank constants
    public static final String RANK_CHIEF = "CHIEF";
    public static final String RANK_INSPECTOR = "INSPECTOR";
    public static final String RANK_SERGEANT = "SERGEANT";
    public static final String RANK_CORPORAL = "CORPORAL";
    public static final String RANK_CONSTABLE = "CONSTABLE";

    // Department constants
    public static final String DEPT_TRAFFIC = "TRAFFIC";
    public static final String DEPT_CRIMINAL_INVESTIGATION = "CRIMINAL_INVESTIGATION";
    public static final String DEPT_PATROL = "PATROL";
    public static final String DEPT_ADMIN = "ADMIN";

    // Core properties
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty badgeNumber = new SimpleStringProperty();
    private final StringProperty rank = new SimpleStringProperty();
    private final IntegerProperty rankLevel = new SimpleIntegerProperty();
    private final StringProperty department = new SimpleStringProperty();
    private final StringProperty stationAssigned = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> hireDate = new SimpleObjectProperty<>();
    private final StringProperty supervisorName = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    // User reference fields (from users table)
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final BooleanProperty active = new SimpleBooleanProperty();
    private final ObjectProperty<LocalDateTime> lastLogin = new SimpleObjectProperty<>();
    private final StringProperty profileImage = new SimpleStringProperty();

    // Computed properties
    private final StringProperty rankDisplayProperty = new SimpleStringProperty();
    private final StringProperty departmentDisplayProperty = new SimpleStringProperty();
    private final StringProperty formattedHireDateProperty = new SimpleStringProperty();
    private final StringProperty yearsOfServiceProperty = new SimpleStringProperty();

    /**
     * Default constructor.
     */
    public PoliceOfficer() {
        super();
        createdAt.set(LocalDateTime.now());
        updatedAt.set(LocalDateTime.now());
        active.set(true);
        rankLevel.set(5);

        rank.addListener((obs, oldVal, newVal) -> updateRankDisplay());
        department.addListener((obs, oldVal, newVal) -> updateDepartmentDisplay());
        hireDate.addListener((obs, oldVal, newVal) -> updateDateProperties());

        updateRankDisplay();
        updateDepartmentDisplay();
        updateDateProperties();
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateRankDisplay() {
        String currentRank = rank.get();
        switch (currentRank) {
            case RANK_CHIEF:
                rankDisplayProperty.set("Chief of Police");
                rankLevel.set(1);
                break;
            case RANK_INSPECTOR:
                rankDisplayProperty.set("Inspector");
                rankLevel.set(2);
                break;
            case RANK_SERGEANT:
                rankDisplayProperty.set("Sergeant");
                rankLevel.set(3);
                break;
            case RANK_CORPORAL:
                rankDisplayProperty.set("Corporal");
                rankLevel.set(4);
                break;
            case RANK_CONSTABLE:
                rankDisplayProperty.set("Constable");
                rankLevel.set(5);
                break;
            default:
                rankDisplayProperty.set(currentRank != null ? currentRank : "Unknown");
                rankLevel.set(9);
        }
    }

    private void updateDepartmentDisplay() {
        String currentDept = department.get();
        switch (currentDept) {
            case DEPT_TRAFFIC:
                departmentDisplayProperty.set("Traffic Division");
                break;
            case DEPT_CRIMINAL_INVESTIGATION:
                departmentDisplayProperty.set("Criminal Investigation");
                break;
            case DEPT_PATROL:
                departmentDisplayProperty.set("Patrol Division");
                break;
            case DEPT_ADMIN:
                departmentDisplayProperty.set("Administration");
                break;
            default:
                departmentDisplayProperty.set(currentDept != null ? currentDept : "Unknown");
        }
    }

    private void updateDateProperties() {
        LocalDate hire = hireDate.get();
        if (hire != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            formattedHireDateProperty.set(hire.format(formatter));

            int years = LocalDate.now().getYear() - hire.getYear();
            yearsOfServiceProperty.set(years + " year" + (years != 1 ? "s" : ""));
        } else {
            formattedHireDateProperty.set("");
            yearsOfServiceProperty.set("");
        }
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public String getDisplayName() {
        return rankDisplayProperty.get() + " " + fullName.get() + " (" + badgeNumber.get() + ")";
    }

    public boolean isTrafficOfficer() {
        return DEPT_TRAFFIC.equals(department.get());
    }

    public boolean isInvestigator() {
        return DEPT_CRIMINAL_INVESTIGATION.equals(department.get());
    }

    public boolean isActiveOfficer() {
        return active.get() && hireDate.get() != null && !hireDate.get().isAfter(LocalDate.now());
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public int getUserId() { return userId.get(); }
    public void setUserId(int value) { userId.set(value); }
    public IntegerProperty userIdProperty() { return userId; }

    public String getBadgeNumber() { return badgeNumber.get(); }
    public void setBadgeNumber(String value) { badgeNumber.set(value); }
    public StringProperty badgeNumberProperty() { return badgeNumber; }

    public String getRank() { return rank.get(); }
    public void setRank(String value) { rank.set(value); }
    public StringProperty rankProperty() { return rank; }

    public int getRankLevel() { return rankLevel.get(); }
    public void setRankLevel(int value) { rankLevel.set(value); }
    public IntegerProperty rankLevelProperty() { return rankLevel; }

    public String getDepartment() { return department.get(); }
    public void setDepartment(String value) { department.set(value); }
    public StringProperty departmentProperty() { return department; }

    public String getStationAssigned() { return stationAssigned.get(); }
    public void setStationAssigned(String value) { stationAssigned.set(value); }
    public StringProperty stationAssignedProperty() { return stationAssigned; }

    public LocalDate getHireDate() { return hireDate.get(); }
    public void setHireDate(LocalDate value) { hireDate.set(value); }
    public ObjectProperty<LocalDate> hireDateProperty() { return hireDate; }

    public String getSupervisorName() { return supervisorName.get(); }
    public void setSupervisorName(String value) { supervisorName.set(value); }
    public StringProperty supervisorNameProperty() { return supervisorName; }

    public String getPhone() { return phone.get(); }
    public void setPhone(String value) { phone.set(value); }
    public StringProperty phoneProperty() { return phone; }

    public String getAddress() { return address.get(); }
    public void setAddress(String value) { address.set(value); }
    public StringProperty addressProperty() { return address; }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime value) { createdAt.set(value); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime value) { updatedAt.set(value); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }

    // User fields
    public String getUsername() { return username.get(); }
    public void setUsername(String value) { username.set(value); }
    public StringProperty usernameProperty() { return username; }

    public String getFullName() { return fullName.get(); }
    public void setFullName(String value) { fullName.set(value); }
    public StringProperty fullNameProperty() { return fullName; }

    public String getEmail() { return email.get(); }
    public void setEmail(String value) { email.set(value); }
    public StringProperty emailProperty() { return email; }

    public boolean isActive() { return active.get(); }
    public void setActive(boolean value) { active.set(value); }
    public BooleanProperty activeProperty() { return active; }

    public LocalDateTime getLastLogin() { return lastLogin.get(); }
    public void setLastLogin(LocalDateTime value) { lastLogin.set(value); }
    public ObjectProperty<LocalDateTime> lastLoginProperty() { return lastLogin; }

    public String getProfileImage() { return profileImage.get(); }
    public void setProfileImage(String value) { profileImage.set(value); }
    public StringProperty profileImageProperty() { return profileImage; }

    // Computed property getters
    public String getRankDisplay() { return rankDisplayProperty.get(); }
    public StringProperty rankDisplayProperty() { return rankDisplayProperty; }

    public String getDepartmentDisplay() { return departmentDisplayProperty.get(); }
    public StringProperty departmentDisplayProperty() { return departmentDisplayProperty; }

    public String getFormattedHireDate() { return formattedHireDateProperty.get(); }
    public StringProperty formattedHireDateProperty() { return formattedHireDateProperty; }

    public String getYearsOfService() { return yearsOfServiceProperty.get(); }
    public StringProperty yearsOfServiceProperty() { return yearsOfServiceProperty; }

    // ============================================
    // OVERRIDE METHODS
    // ============================================

    @Override
    public String toString() {
        return getRankDisplay() + " " + getFullName() + " (" + getBadgeNumber() + ")";
    }

    /**
     * Creates a copy of this police officer.
     *
     * @return a new PoliceOfficer instance
     */
    public PoliceOfficer copy() {
        PoliceOfficer copy = new PoliceOfficer();
        copy.setId(this.getId());
        copy.setUserId(this.getUserId());
        copy.setBadgeNumber(this.getBadgeNumber());
        copy.setRank(this.getRank());
        copy.setRankLevel(this.getRankLevel());
        copy.setDepartment(this.getDepartment());
        copy.setStationAssigned(this.getStationAssigned());
        copy.setHireDate(this.getHireDate());
        copy.setSupervisorName(this.getSupervisorName());
        copy.setPhone(this.getPhone());
        copy.setAddress(this.getAddress());
        copy.setUsername(this.getUsername());
        copy.setFullName(this.getFullName());
        copy.setEmail(this.getEmail());
        copy.setActive(this.isActive());
        copy.setLastLogin(this.getLastLogin());
        copy.setProfileImage(this.getProfileImage());
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}