package dao;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.PoliceOfficer;

/**
 * PoliceOfficerDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0 */
public class PoliceOfficerDAO extends BaseDAO<PoliceOfficer> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public PoliceOfficerDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public PoliceOfficer findById(int id) throws SQLException {
        List<PoliceOfficer> results = viewLoader.loadViewWithCondition("vw_police_officers", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    public PoliceOfficer findByUserId(int userId) throws SQLException {
        List<PoliceOfficer> results = viewLoader.loadViewWithCondition("vw_police_officers", "user_id = ?", userId);
        return results.isEmpty() ? null : results.get(0);
    }

    public PoliceOfficer findByBadgeNumber(String badgeNumber) throws SQLException {
        List<PoliceOfficer> results = viewLoader.loadViewWithCondition("vw_police_officers", "badge_number = ?", badgeNumber);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<PoliceOfficer> findAll() throws SQLException {
        return viewLoader.loadView("vw_police_officers");
    }

    public List<PoliceOfficer> findByRank(String rank) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_police_officers", "rank = ? ORDER BY full_name", rank);
    }

    public List<PoliceOfficer> findByDepartment(String department) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_police_officers", "department = ? ORDER BY full_name", department);
    }

    @Override
    public boolean insert(PoliceOfficer entity) throws SQLException {
        Integer officerId = procedureCaller.executeAddPoliceOfficer(
                entity.getUserId(),
                entity.getBadgeNumber(),
                entity.getRank(),
                entity.getDepartment(),
                entity.getStationAssigned(),
                entity.getHireDate() != null ? Date.valueOf(entity.getHireDate()) : null,
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