package utils;

import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * SessionManager manages the current user session across the application.
 * Stores user information, role, and module-specific IDs.
 * Implements Singleton pattern for global access.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class SessionManager {

    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());
    private static SessionManager instance;

    // Session data
    private int userId;
    private String username;
    private String userRole;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime loginTime;

    // Role-specific IDs
    private int customerId;
    private int workshopId;
    private int insuranceProviderId;
    private int policeOfficerId;

    // Police-specific fields
    private String badgeNumber;
    private String rank;

    /**
     * Private constructor for singleton pattern.
     */
    private SessionManager() {
        this.customerId = -1;
        this.workshopId = -1;
        this.insuranceProviderId = -1;
        this.policeOfficerId = -1;
    }

    /**
     * Gets the singleton instance of SessionManager.
     *
     * @return the SessionManager instance
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Creates a new user session.
     *
     * @param userId   the user ID
     * @param username the username
     * @param role     the user role
     * @param fullName the user's full name
     * @param email    the user's email
     */
    public void createSession(int userId, String username, String role, String fullName, String email) {
        this.userId = userId;
        this.username = username;
        this.userRole = role;
        this.fullName = fullName;
        this.email = email;
        this.loginTime = LocalDateTime.now();

        // Reset role-specific IDs
        this.customerId = -1;
        this.workshopId = -1;
        this.insuranceProviderId = -1;
        this.policeOfficerId = -1;

        LOGGER.info("Session created for user: " + username + " (Role: " + role + ")");
    }

    /**
     * Clears the current session (logout).
     */
    public void clearSession() {
        this.userId = 0;
        this.username = null;
        this.userRole = null;
        this.fullName = null;
        this.email = null;
        this.phone = null;
        this.address = null;
        this.loginTime = null;
        this.customerId = -1;
        this.workshopId = -1;
        this.insuranceProviderId = -1;
        this.policeOfficerId = -1;
        this.badgeNumber = null;
        this.rank = null;

        LOGGER.info("Session cleared");
    }

    /**
     * Checks if a user is logged in.
     *
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return userId > 0 && username != null && userRole != null;
    }

    /**
     * Checks if the current user has a specific role.
     *
     * @param role the role to check
     * @return true if user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        return userRole != null && userRole.equals(role);
    }

    /**
     * Checks if the current user is an admin.
     *
     * @return true if admin, false otherwise
     */
    public boolean isAdmin() {
        return "ADMIN".equals(userRole);
    }

    /**
     * Checks if the current user is police.
     *
     * @return true if police, false otherwise
     */
    public boolean isPolice() {
        return "POLICE".equals(userRole);
    }

    /**
     * Checks if the current user is a customer.
     *
     * @return true if customer, false otherwise
     */
    public boolean isCustomer() {
        return "CUSTOMER".equals(userRole);
    }

    /**
     * Checks if the current user is a workshop.
     *
     * @return true if workshop, false otherwise
     */
    public boolean isWorkshop() {
        return "WORKSHOP".equals(userRole);
    }

    /**
     * Checks if the current user is insurance.
     *
     * @return true if insurance, false otherwise
     */
    public boolean isInsurance() {
        return "INSURANCE".equals(userRole);
    }

    /**
     * Gets the session duration in minutes.
     *
     * @return session duration in minutes, or 0 if not logged in
     */
    public long getSessionDurationMinutes() {
        if (loginTime == null) return 0;
        return java.time.Duration.between(loginTime, LocalDateTime.now()).toMinutes();
    }

    /**
     * Validates if the session has expired (e.g., after 8 hours).
     *
     * @return true if expired, false otherwise
     */
    public boolean isSessionExpired() {
        if (loginTime == null) return true;
        long hours = java.time.Duration.between(loginTime, LocalDateTime.now()).toHours();
        return hours >= 8; // 8-hour session timeout
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getUserRole() { return userRole; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDateTime getLoginTime() { return loginTime; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getWorkshopId() { return workshopId; }
    public void setWorkshopId(int workshopId) { this.workshopId = workshopId; }

    public int getInsuranceProviderId() { return insuranceProviderId; }
    public void setInsuranceProviderId(int insuranceProviderId) { this.insuranceProviderId = insuranceProviderId; }

    public int getPoliceOfficerId() { return policeOfficerId; }
    public void setPoliceOfficerId(int policeOfficerId) { this.policeOfficerId = policeOfficerId; }

    public String getBadgeNumber() { return badgeNumber; }
    public void setBadgeNumber(String badgeNumber) { this.badgeNumber = badgeNumber; }

    public String getRank() { return rank; }
    public void setRank(String rank) { this.rank = rank; }
}