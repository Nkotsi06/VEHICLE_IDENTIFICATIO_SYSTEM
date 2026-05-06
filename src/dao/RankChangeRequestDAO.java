package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_rank_change_requests", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToRankChangeRequest(results.get(0));
    }

    @Override
    public List<RankChangeRequest> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_rank_change_requests");
        return mapMapsToRankChangeRequests(results);
    }

    public List<RankChangeRequest> findByOfficerId(int officerId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_rank_change_requests", "officer_id = ? ORDER BY created_at DESC", officerId);
        return mapMapsToRankChangeRequests(results);
    }

    public List<RankChangeRequest> findByStatus(String status) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_rank_change_requests", "status = ? ORDER BY created_at DESC", status);
        return mapMapsToRankChangeRequests(results);
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

    /**
     * Converts a List of Maps to a List of RankChangeRequest objects.
     *
     * @param maps the list of maps from the view loader
     * @return list of RankChangeRequest objects
     */
    private List<RankChangeRequest> mapMapsToRankChangeRequests(List<Map<String, Object>> maps) {
        List<RankChangeRequest> requests = new ArrayList<>();
        if (maps == null) {
            return requests;
        }
        for (Map<String, Object> map : maps) {
            RankChangeRequest request = mapMapToRankChangeRequest(map);
            if (request != null) {
                requests.add(request);
            }
        }
        return requests;
    }

    /**
     * Converts a Map to a RankChangeRequest object.
     *
     * @param map the map from the view loader
     * @return RankChangeRequest object
     */
    private RankChangeRequest mapMapToRankChangeRequest(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        RankChangeRequest request = new RankChangeRequest();

        request.setId(getIntValue(map, "id"));
        request.setOfficerId(getIntValue(map, "officer_id"));
        request.setOfficerName(getStringValue(map, "officer_name"));
        request.setCurrentRank(getStringValue(map, "current_rank"));
        request.setRequestedRank(getStringValue(map, "requested_rank"));
        request.setReason(getStringValue(map, "reason"));
        request.setStatus(getStringValue(map, "status"));
        request.setReviewNotes(getStringValue(map, "review_notes"));

        int reviewedBy = getIntValue(map, "reviewed_by");
        if (reviewedBy > 0) {
            request.setReviewedBy(reviewedBy);
        }

        String reviewerName = getStringValue(map, "reviewer_name");
        if (reviewerName != null && !reviewerName.isEmpty()) {
            request.setReviewerName(reviewerName);
        }

        request.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        request.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));
        request.setReviewedAt(getLocalDateTimeValue(map, "reviewed_at"));

        return request;
    }

    /**
     * Helper method to safely get Integer values from Map.
     */
    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    /**
     * Helper method to safely get String values from Map.
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * Helper method to safely get LocalDateTime values from Map.
     */
    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        return null;
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