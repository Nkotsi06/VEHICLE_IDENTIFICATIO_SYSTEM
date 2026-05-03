package dao;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import models.PoliceOfficer;

public class PoliceOfficerDAO extends BaseDAO<PoliceOfficer> {

    @Override
    public PoliceOfficer findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_police_officers WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    public PoliceOfficer findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM vw_police_officers WHERE user_id = ?";
        return executeQuerySingle(sql, userId);
    }

    public PoliceOfficer findByBadgeNumber(String badgeNumber) throws SQLException {
        String sql = "SELECT * FROM vw_police_officers WHERE badge_number = ?";
        return executeQuerySingle(sql, badgeNumber);
    }

    @Override
    public List<PoliceOfficer> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_police_officers ORDER BY rank_level, full_name";
        return executeQuery(sql);
    }

    public List<PoliceOfficer> findByRank(String rank) throws SQLException {
        String sql = "SELECT * FROM vw_police_officers WHERE rank = ? ORDER BY full_name";
        return executeQuery(sql, rank);
    }

    public List<PoliceOfficer> findByDepartment(String department) throws SQLException {
        String sql = "SELECT * FROM vw_police_officers WHERE department = ? ORDER BY full_name";
        return executeQuery(sql, department);
    }

    @Override
    public boolean insert(PoliceOfficer entity) throws SQLException {
        String sql = "INSERT INTO police_officers (user_id, badge_number, rank, rank_level, department, station_assigned, hire_date, supervisor_name, phone, address) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getUserId(),
                entity.getBadgeNumber(),
                entity.getRank(),
                entity.getRankLevel(),
                entity.getDepartment(),
                entity.getStationAssigned(),
                entity.getHireDate() != null ? Date.valueOf(entity.getHireDate()) : null,
                entity.getSupervisorName(),
                entity.getPhone(),
                entity.getAddress()
        );
        return result > 0;
    }

    @Override
    public boolean update(PoliceOfficer entity) throws SQLException {
        String sql = "UPDATE police_officers SET rank = ?, rank_level = ?, department = ?, station_assigned = ?, " +
                "supervisor_name = ?, phone = ?, address = ? WHERE id = ?";
        int result = executeUpdate(sql,
                entity.getRank(),
                entity.getRankLevel(),
                entity.getDepartment(),
                entity.getStationAssigned(),
                entity.getSupervisorName(),
                entity.getPhone(),
                entity.getAddress(),
                entity.getId()
        );

        if (result > 0) {
            String updateUserSql = "UPDATE users SET full_name = ?, email = ?, phone = ?, address = ? WHERE id = ?";
            executeUpdate(updateUserSql,
                    entity.getFullName(),
                    entity.getEmail(),
                    entity.getPhone(),
                    entity.getAddress(),
                    entity.getUserId()
            );
        }

        return result > 0;
    }

    public boolean updateRank(int officerId, String newRank, int newRankLevel) throws SQLException {
        String sql = "UPDATE police_officers SET rank = ?, rank_level = ? WHERE id = ?";
        int result = executeUpdate(sql, newRank, newRankLevel, officerId);
        return result > 0;
    }

    /**
     * Update police officer's profile image path
     * @param officerId The ID of the police officer
     * @param imagePath The file path to the profile image
     * @return true if update was successful, false otherwise
     */
    public boolean updateProfileImage(int officerId, String imagePath) throws SQLException {
        String sql = "UPDATE police_officers SET profile_image = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, imagePath);
            ps.setInt(2, officerId);
            int result = ps.executeUpdate();

            // Also log the activity - could be handled by trigger
            System.out.println("Profile image updated for officer ID: " + officerId + " - Path: " + imagePath);

            return result > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    /**
     * Get profile image path for a police officer
     * @param officerId The ID of the police officer
     * @return The file path to the profile image, or null if not set
     */
    public String getProfileImagePath(int officerId) throws SQLException {
        String sql = "SELECT profile_image FROM police_officers WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, officerId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("profile_image");
            }
            return null;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    /**
     * Clear profile image for a police officer
     * @param officerId The ID of the police officer
     * @return true if update was successful, false otherwise
     */
    public boolean clearProfileImage(int officerId) throws SQLException {
        String sql = "UPDATE police_officers SET profile_image = NULL WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, officerId);
            return ps.executeUpdate() > 0;
        } finally {
            closeResources(null, ps, conn);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM police_officers WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public List<String> getAllRanks() throws SQLException {
        List<String> ranks = new ArrayList<>();
        String sql = "SELECT rank_name FROM police_ranks ORDER BY rank_level";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                ranks.add(rs.getString("rank_name"));
            }
            return ranks;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public List<String> getAllDepartments() throws SQLException {
        List<String> departments = new ArrayList<>();
        String sql = "SELECT department_name FROM police_departments ORDER BY department_name";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                departments.add(rs.getString("department_name"));
            }
            return departments;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public boolean requiresApprovalForRank(String rankName) throws SQLException {
        String sql = "SELECT requires_approval FROM police_ranks WHERE rank_name = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, rankName);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("requires_approval");
            }
            return false;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    /**
     * Get rank level for a given rank name
     * @param rankName The name of the rank
     * @return The rank level number, or 0 if not found
     */
    public int getRankLevel(String rankName) throws SQLException {
        String sql = "SELECT rank_level FROM police_ranks WHERE rank_name = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, rankName);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("rank_level");
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
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