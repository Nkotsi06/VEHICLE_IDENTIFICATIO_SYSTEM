package utils;

import java.time.LocalDateTime;

public class SessionManager {

    private static SessionManager instance;
    private int userId;
    private String username;
    private String userRole;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private LocalDateTime loginTime;
    private int customerId;
    private int workshopId;
    private int insuranceProviderId;
    private int policeOfficerId;
    private String badgeNumber;
    private String rank;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void createSession(int userId, String username, String role, String fullName, String email) {
        this.userId = userId;
        this.username = username;
        this.userRole = role;
        this.fullName = fullName;
        this.email = email;
        this.loginTime = LocalDateTime.now();
        this.customerId = -1;
        this.workshopId = -1;
        this.insuranceProviderId = -1;
        this.policeOfficerId = -1;
    }

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
    }

    public boolean isLoggedIn() {
        return userId > 0 && username != null && userRole != null;
    }

    public boolean hasRole(String role) {
        return userRole != null && userRole.equals(role);
    }

    public boolean isAdmin() {
        return "ADMIN".equals(userRole);
    }

    public boolean isPolice() {
        return "POLICE".equals(userRole);
    }

    public boolean isCustomer() {
        return "CUSTOMER".equals(userRole);
    }

    public boolean isWorkshop() {
        return "WORKSHOP".equals(userRole);
    }

    public boolean isInsurance() {
        return "INSURANCE".equals(userRole);
    }

    // Getters and Setters
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