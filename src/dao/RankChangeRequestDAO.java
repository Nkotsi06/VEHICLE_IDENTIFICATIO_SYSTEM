package dao;

import java.sql.*;
import java.util.List;
import models.RankChangeRequest;
import models.PoliceOfficer;  // ADD THIS IMPORT
import dao.PoliceOfficerDAO;   // ADD THIS IMPORT

public class RankChangeRequestDAO extends BaseDAO<RankChangeRequest> {

    @Override
    public RankChangeRequest findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_rank_change_requests WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<RankChangeRequest> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_rank_change_requests ORDER BY created_at DESC";
        return executeQuery(sql);
    }

    public List<RankChangeRequest> findByOfficerId(int officerId) throws SQLException {
        String sql = "SELECT * FROM vw_rank_change_requests WHERE officer_id = ? ORDER BY created_at DESC";
        return executeQuery(sql, officerId);
    }

    public List<RankChangeRequest> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM vw_rank_change_requests WHERE status = ? ORDER BY created_at DESC";
        return executeQuery(sql, status);
    }

    public List<RankChangeRequest> findPendingRequests() throws SQLException {
        return findByStatus("PENDING");
    }

    @Override
    public boolean insert(RankChangeRequest entity) throws SQLException {
        String sql = "INSERT INTO rank_change_requests (officer_id, current_rank, requested_rank, reason, status) " +
                "VALUES (?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getOfficerId(),
                entity.getCurrentRank(),
                entity.getRequestedRank(),
                entity.getReason(),
                "PENDING"
        );
        return result > 0;
    }

    public boolean approveRequest(int requestId, int reviewedBy, String reviewNotes) throws SQLException {
        String sql = "UPDATE rank_change_requests SET status = 'APPROVED', reviewed_by = ?, review_notes = ? WHERE id = ?";
        int result = executeUpdate(sql, reviewedBy, reviewNotes, requestId);

        if (result > 0) {
            RankChangeRequest request = findById(requestId);
            if (request != null) {
                PoliceOfficerDAO officerDAO = new PoliceOfficerDAO();
                PoliceOfficer officer = officerDAO.findById(request.getOfficerId());
                if (officer != null) {
                    officerDAO.updateRank(request.getOfficerId(), request.getRequestedRank(),
                            getRankLevel(request.getRequestedRank()));
                }
            }
        }
        return result > 0;
    }

    public boolean rejectRequest(int requestId, int reviewedBy, String reviewNotes) throws SQLException {
        String sql = "UPDATE rank_change_requests SET status = 'REJECTED', reviewed_by = ?, review_notes = ? WHERE id = ?";
        int result = executeUpdate(sql, reviewedBy, reviewNotes, requestId);
        return result > 0;
    }

    private int getRankLevel(String rankName) throws SQLException {
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
            return 1;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public boolean update(RankChangeRequest entity) throws SQLException {
        String sql = "UPDATE rank_change_requests SET status = ?, reviewed_by = ?, review_notes = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getStatus(), entity.getReviewedBy(), entity.getReviewNotes(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM rank_change_requests WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    @Override
    protected RankChangeRequest mapRow(ResultSet rs) throws SQLException {
        RankChangeRequest request = new RankChangeRequest();
        request.setId(rs.getInt("id"));
        request.setOfficerId(rs.getInt("officer_id"));
        request.setOfficerName(rs.getString("officer_name"));
        request.setCurrentRank(rs.getString("current_rank"));
        request.setRequestedRank(rs.getString("requested_rank"));
        request.setReason(rs.getString("reason"));
        request.setStatus(rs.getString("status"));

        int reviewedBy = rs.getInt("reviewed_by");
        if (reviewedBy > 0) {
            request.setReviewedBy(reviewedBy);
            request.setReviewerName(rs.getString("reviewer_name"));
        }

        request.setReviewNotes(rs.getString("review_notes"));

        if (rs.getTimestamp("created_at") != null) {
            request.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            request.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }

        return request;
    }
}