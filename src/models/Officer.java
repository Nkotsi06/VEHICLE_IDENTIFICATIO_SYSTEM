package models;

import java.time.LocalDate;

public class Officer extends BaseEntity {
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

    public Officer() {
        super();
        this.isActive = true;
    }

    public Officer(String badgeNumber, String rank, String station, String division) {
        this();
        this.badgeNumber = badgeNumber;
        this.rank = rank;
        this.station = station;
        this.division = division;
        this.joinedDate = LocalDate.now();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBadgeNumber() {
        return badgeNumber;
    }

    public void setBadgeNumber(String badgeNumber) {
        this.badgeNumber = badgeNumber;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public LocalDate getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDate joinedDate) {
        this.joinedDate = joinedDate;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getRankPriority() {
        switch (rank) {
            case "CHIEF": return "1";
            case "INSPECTOR": return "2";
            case "SERGEANT": return "3";
            case "CORPORAL": return "4";
            case "CONSTABLE": return "5";
            default: return "9";
        }
    }

    @Override
    public String toString() {
        return rank + " " + fullName + " - " + badgeNumber;
    }
}