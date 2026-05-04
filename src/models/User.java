package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * User model representing system users.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class
User extends BaseEntity {

    // Core fields
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

    // Role constants
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_POLICE = "POLICE";
    public static final String ROLE_CUSTOMER = "CUSTOMER";
    public static final String ROLE_WORKSHOP = "WORKSHOP";
    public static final String ROLE_INSURANCE = "INSURANCE";

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
    private final StringProperty roleDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusDisplayProperty = new SimpleStringProperty();
    private final StringProperty statusColorProperty = new SimpleStringProperty();

    /**
     * Default constructor - initializes with ACTIVE status.
     */
    public User() {
        super();
        this.isActive = true;

        activeProperty.set(true);
        updateRoleDisplay();
        updateStatusDisplay();

        roleProperty.addListener((obs, oldVal, newVal) -> updateRoleDisplay());
        activeProperty.addListener((obs, oldVal, newVal) -> updateStatusDisplay());
    }

    /**
     * Constructor for creating a new user.
     *
     * @param username the username
     * @param password the password
     * @param role     the user role
     * @param fullName the full name
     * @param email    the email address
     */
    public User(String username, String password, String role, String fullName, String email) {
        this();
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;

        usernameProperty.set(username);
        fullNameProperty.set(fullName);
        emailProperty.set(email);
        roleProperty.set(role);
    }

    // ============================================
    // PRIVATE UPDATE METHODS
    // ============================================

    private void updateRoleDisplay() {
        switch (role) {
            case ROLE_ADMIN:
                roleDisplayProperty.set("Administrator");
                break;
            case ROLE_POLICE:
                roleDisplayProperty.set("Police Officer");
                break;
            case ROLE_CUSTOMER:
                roleDisplayProperty.set("Customer");
                break;
            case ROLE_WORKSHOP:
                roleDisplayProperty.set("Workshop");
                break;
            case ROLE_INSURANCE:
                roleDisplayProperty.set("Insurance Provider");
                break;
            default:
                roleDisplayProperty.set(role);
        }
    }

    private void updateStatusDisplay() {
        if (isActive) {
            statusDisplayProperty.set("Active");
            statusColorProperty.set("#4CAF50");
        } else {
            statusDisplayProperty.set("Inactive");
            statusColorProperty.set("#9E9E9E");
        }
    }

    // ============================================
    // GETTERS AND SETTERS WITH PROPERTY UPDATES
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

    public String getRoleDisplay() {
        return roleDisplayProperty.get();
    }

    public StringProperty roleDisplayProperty() {
        return roleDisplayProperty;
    }

    public String getStatusDisplay() {
        return statusDisplayProperty.get();
    }

    public StringProperty statusDisplayProperty() {
        return statusDisplayProperty;
    }

    public String getStatusColor() {
        return statusColorProperty.get();
    }

    public StringProperty statusColorProperty() {
        return statusColorProperty;
    }

    // ============================================
    // BUSINESS LOGIC METHODS
    // ============================================

    public String getFormattedLastLogin() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return lastLogin != null ? lastLogin.format(formatter) : "Never";
    }

    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    public boolean isPolice() {
        return ROLE_POLICE.equals(role);
    }

    public boolean isCustomer() {
        return ROLE_CUSTOMER.equals(role);
    }

    public boolean isWorkshop() {
        return ROLE_WORKSHOP.equals(role);
    }

    public boolean isInsurance() {
        return ROLE_INSURANCE.equals(role);
    }

    public void activate() {
        setActive(true);
    }

    public void deactivate() {
        setActive(false);
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
        lastLoginProperty.set(this.lastLogin);
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
        return fullName + " (" + username + ") - " + getRoleDisplay();
    }

    /**
     * Creates a copy of this user.
     *
     * @return a new User instance
     */
    public User copy() {
        User copy = new User();
        copy.setId(this.id);
        copy.setUsername(this.username);
        copy.setPassword(this.password);
        copy.setRole(this.role);
        copy.setFullName(this.fullName);
        copy.setEmail(this.email);
        copy.setPhone(this.phone);
        copy.setAddress(this.address);
        copy.setProfileImage(this.profileImage);
        copy.setActive(this.isActive);
        copy.setLastLogin(this.lastLogin);
        copy.setCreatedAt(this.getCreatedAt());
        copy.setUpdatedAt(this.getUpdatedAt());
        return copy;
    }
}