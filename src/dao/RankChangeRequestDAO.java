package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.RankChangeRequest;

/**
 * RankChangeRequestDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class RankChangeRequestDAO extends BaseDAO<RankChangeRequest> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public RankChangeRequestDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public RankChangeRequest findById(int id) throws SQLException {
        List<RankChangeRequest> results = viewLoader.loadViewWithCondition("vw_rank_change_requests", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<RankChangeRequest> findAll() throws SQLException {
        return viewLoader.loadView("vw_rank_change_requests");
    }

    public List<RankChangeRequest> findByOfficerId(int officerId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_rank_change_requests", "officer_id = ? ORDER BY created_at DESC", officerId);
    }

    public List<RankChangeRequest> findByStatus(String status) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_rank_change_requests", "status = ? ORDER BY created_at DESC", status);
    }

    public List<RankChangeRequest> findPendingRequests() throws SQLException {
        return findByStatus("PENDING");
    }

    @Override
    public boolean insert(RankChangeRequest entity) throws SQLException {
        return procedureCaller.executeInsertRankChangeRequest(
                entity.getOfficerId(),
                entity.getCurrentRank(),
                entity.getRequestedRank(),
                entity.getReason()
        );
    }

    public boolean approveRequest(int requestId, int reviewedBy, String reviewNotes) throws SQLException {
        boolean result = procedureCaller.executeApproveRankChangeRequest(requestId, reviewedBy, reviewNotes);
        if (result) {
            // The procedure handles updating the officer's rank
        }
        return result;
    }

    public boolean rejectRequest(int requestId, int reviewedBy, String reviewNotes) throws SQLException {
        return procedureCaller.executeRejectRankChangeRequest(requestId, reviewedBy, reviewNotes);
    }

    @Override
    public boolean update(RankChangeRequest entity) throws SQLException {
        if ("APPROVED".equals(entity.getStatus())) {
            return approveRequest(entity.getId(), entity.getReviewedBy(), entity.getReviewNotes());
        } else if ("REJECTED".equals(entity.getStatus())) {
            return rejectRequest(entity.getId(), entity.getReviewedBy(), entity.getReviewNotes());
        }
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteRankChangeRequest(id);
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
        if (rs.getTimestamp("reviewed_at") != null) {
            request.setReviewedAt(rs.getTimestamp("reviewed_at").toLocalDateTime());
        }

        return request;
    }
}