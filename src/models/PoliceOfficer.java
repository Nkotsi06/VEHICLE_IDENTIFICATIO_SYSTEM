package models;

import javafx.beans.property.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PoliceOfficer {
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

    // Getters and Setters
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

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime value) { updatedAt.set(value); }

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
}