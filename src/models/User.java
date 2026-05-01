package models;

import java.time.LocalDateTime;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class User extends BaseEntity {
    private int id;
    private String username;
    private String password;
    private String role;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String profileImage;
    private boolean isActive;
    private LocalDateTime lastLogin;

    // JavaFX Properties for TableView binding
    private final StringProperty usernameProperty = new SimpleStringProperty();
    private final StringProperty fullNameProperty = new SimpleStringProperty();
    private final StringProperty emailProperty = new SimpleStringProperty();
    private final StringProperty roleProperty = new SimpleStringProperty();
    private final StringProperty phoneProperty = new SimpleStringProperty();
    private final StringProperty addressProperty = new SimpleStringProperty();
    private final StringProperty profileImageProperty = new SimpleStringProperty();
    private final BooleanProperty activeProperty = new SimpleBooleanProperty();
    private final ObjectProperty<LocalDateTime> lastLoginProperty = new SimpleObjectProperty<>();

    public User() {
        super();
        this.isActive = true;
    }

    public User(String username, String password, String role, String fullName, String email) {
        this();
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;

        this.usernameProperty.set(username);
        this.fullNameProperty.set(fullName);
        this.emailProperty.set(email);
        this.roleProperty.set(role);
        this.activeProperty.set(true);
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
        roleProperty.set(role);
    }

    public StringProperty roleProperty() {
        return roleProperty;
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

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
        profileImageProperty.set(profileImage);
    }

    public StringProperty profileImageProperty() {
        return profileImageProperty;
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

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
        lastLoginProperty.set(lastLogin);
    }

    public ObjectProperty<LocalDateTime> lastLoginProperty() {
        return lastLoginProperty;
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
        return fullName + " (" + username + ") - " + role;
    }
}