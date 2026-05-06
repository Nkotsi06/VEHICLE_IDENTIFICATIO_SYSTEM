package dao;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.PoliceOfficer;
import models.User;

/**
 * PoliceOfficerDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class PoliceOfficerDAO extends BaseDAO<PoliceOfficer> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public PoliceOfficerDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public PoliceOfficer findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_officers", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPoliceOfficer(results.get(0));
    }

    public PoliceOfficer findByUserId(int userId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_officers", "user_id = ?", userId);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPoliceOfficer(results.get(0));
    }

    /**
     * Finds police officer by user ID or creates a default record if not exists
     * @param userId the user ID
     * @return PoliceOfficer object, never null
     * @throws SQLException if database error occurs
     */
    public PoliceOfficer findByUserIdOrCreate(int userId) throws SQLException {
        PoliceOfficer officer = findByUserId(userId);

        if (officer == null) {
            // Check if user exists and has POLICE role
            UserDAO userDAO = new UserDAO();
            User user = userDAO.findById(userId);

            if (user != null && "POLICE".equals(user.getRole())) {
                // Create default police officer record
                officer = new PoliceOfficer();
                officer.setUserId(userId);
                officer.setBadgeNumber("BP" + String.format("%05d", userId));
                officer.setRank("OFFICER");
                officer.setRankLevel(1);
                officer.setDepartment("TRAFFIC");
                officer.setStationAssigned("CENTRAL POLICE STATION");
                officer.setHireDate(LocalDate.now());
                officer.setSupervisorName("CHIEF INSPECTOR");
                officer.setPhone("+266 5000 0000");
                officer.setAddress("POLICE HEADQUARTERS, MASERU");
                officer.setUsername(user.getUsername());
                officer.setFullName(user.getFullName());
                officer.setEmail(user.getEmail());
                officer.setActive(true);

                boolean created = insert(officer);
                if (created) {
                    officer = findByUserId(userId);
                }
            }
        }

        return officer;
    }

    public PoliceOfficer findByBadgeNumber(String badgeNumber) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_officers", "badge_number = ?", badgeNumber);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPoliceOfficer(results.get(0));
    }

    @Override
    public List<PoliceOfficer> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_police_officers");
        return mapMapsToPoliceOfficers(results);
    }

    public List<PoliceOfficer> findByRank(String rank) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_officers", "rank = ? ORDER BY full_name", rank);
        return mapMapsToPoliceOfficers(results);
    }

    public List<PoliceOfficer> findByDepartment(String department) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_police_officers", "department = ? ORDER BY full_name", department);
        return mapMapsToPoliceOfficers(results);
    }

    @Override
    public boolean insert(PoliceOfficer entity) throws SQLException {
        Integer officerId = procedureCaller.executeAddPoliceOfficer(
                entity.getUserId(),
                entity.getBadgeNumber(),
                entity.getRank(),
                entity.getDepartment(),
                entity.getStationAssigned(),
                entity.getHireDate(),
                entity.getSupervisorName(),
                entity.getPhone()
        );
        if (officerId != null && officerId > 0) {
            entity.setId(officerId);
            return true;
        }
        return false;
    }

    @Override
    public boolean update(PoliceOfficer entity) throws SQLException {
        return procedureCaller.executeUpdatePoliceOfficer(
                entity.getId(),
                entity.getRank(),
                entity.getRankLevel(),
                entity.getDepartment(),
                entity.getStationAssigned(),
                entity.getSupervisorName(),
                entity.getPhone(),
                entity.getAddress()
        );
    }

    public boolean updateRank(int officerId, String newRank, int newRankLevel) throws SQLException {
        return procedureCaller.executeUpdatePoliceOfficerRank(officerId, newRank, newRankLevel);
    }

    public boolean updateProfileImage(int officerId, String imagePath) throws SQLException {
        return procedureCaller.executeUpdatePoliceOfficerProfileImage(officerId, imagePath);
    }

    public String getProfileImagePath(int officerId) throws SQLException {
        PoliceOfficer officer = findById(officerId);
        return officer != null ? officer.getProfileImage() : null;
    }

    public boolean clearProfileImage(int officerId) throws SQLException {
        return procedureCaller.executeClearPoliceOfficerProfileImage(officerId);
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeletePoliceOfficer(id);
    }

    public List<String> getAllRanks() throws SQLException {
        return viewLoader.loadStringListFromView("police_ranks", "rank_name", "rank_level");
    }

    public List<String> getAllDepartments() throws SQLException {
        return viewLoader.loadStringListFromView("police_departments", "department_name", "department_name");
    }

    public boolean requiresApprovalForRank(String rankName) throws SQLException {
        return viewLoader.getBooleanValueFromTable("police_ranks", "requires_approval", "rank_name = ?", rankName);
    }

    public int getRankLevel(String rankName) throws SQLException {
        return viewLoader.getIntValueFromTable("police_ranks", "rank_level", "rank_name = ?", rankName);
    }

    /**
     * Converts a List of Maps to a List of PoliceOfficer objects.
     */
    private List<PoliceOfficer> mapMapsToPoliceOfficers(List<Map<String, Object>> maps) {
        List<PoliceOfficer> officers = new ArrayList<>();
        if (maps == null) {
            return officers;
        }
        for (Map<String, Object> map : maps) {
            PoliceOfficer officer = mapMapToPoliceOfficer(map);
            if (officer != null) {
                officers.add(officer);
            }
        }
        return officers;
    }

    /**
     * Converts a Map to a PoliceOfficer object.
     */
    private PoliceOfficer mapMapToPoliceOfficer(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        PoliceOfficer officer = new PoliceOfficer();

        officer.setId(getIntValue(map, "id"));
        officer.setUserId(getIntValue(map, "user_id"));
        officer.setBadgeNumber(getStringValue(map, "badge_number"));
        officer.setRank(getStringValue(map, "rank"));
        officer.setRankLevel(getIntValue(map, "rank_level"));
        officer.setDepartment(getStringValue(map, "department"));
        officer.setStationAssigned(getStringValue(map, "station_assigned"));
        officer.setSupervisorName(getStringValue(map, "supervisor_name"));
        officer.setPhone(getStringValue(map, "phone"));
        officer.setAddress(getStringValue(map, "address"));
        officer.setUsername(getStringValue(map, "username"));
        officer.setFullName(getStringValue(map, "full_name"));
        officer.setEmail(getStringValue(map, "email"));
        officer.setProfileImage(getStringValue(map, "profile_image"));

        officer.setHireDate(getLocalDateValue(map, "hire_date"));
        officer.setLastLogin(getLocalDateTimeValue(map, "last_login"));
        officer.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        officer.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return officer;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private LocalDate getLocalDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Date) return ((java.sql.Date) value).toLocalDate();
        if (value instanceof LocalDate) return (LocalDate) value;
        return null;
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
    }

    @Override
    protected PoliceOfficer mapRow(ResultSet rs) throws SQLException {
        PoliceOfficer officer = new PoliceOfficer();
        officer.setId(rs.getInt("id"));
        officer.setUserId(rs.getInt("user_id"));
        officer.setBadgeNumber(rs.getString("badge_number"));
        officer.setRank(rs.getString("rank"));
        officer.setRankLevel(rs.getInt("rank_level"));
        officer.setDepartment(rs.getString("department"));
        officer.setStationAssigned(rs.getString("station_assigned"));

        Date hireDate = rs.getDate("hire_date");
        if (hireDate != null) {
            officer.setHireDate(hireDate.toLocalDate());
        }

        officer.setSupervisorName(rs.getString("supervisor_name"));
        officer.setPhone(rs.getString("phone"));
        officer.setAddress(rs.getString("address"));

        if (rs.getTimestamp("created_at") != null) {
            officer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            officer.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }

        officer.setUsername(rs.getString("username"));
        officer.setFullName(rs.getString("full_name"));
        officer.setEmail(rs.getString("email"));

        if (rs.getTimestamp("last_login") != null) {
            officer.setLastLogin(rs.getTimestamp("last_login").toLocalDateTime());
        }

        officer.setProfileImage(rs.getString("profile_image"));

        return officer;
    }
}