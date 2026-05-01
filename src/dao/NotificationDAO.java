package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.Notification;

public class NotificationDAO extends BaseDAO<Notification> {

    @Override
    public Notification findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_notifications WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<Notification> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_notifications ORDER BY created_at DESC";
        return executeQuery(sql);
    }

    public List<Notification> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM vw_notifications WHERE user_id = ? ORDER BY created_at DESC";
        return executeQuery(sql, userId);
    }

    public List<Notification> findUnreadByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM vw_notifications WHERE user_id = ? AND is_read = false ORDER BY created_at DESC";
        return executeQuery(sql, userId);
    }

    public List<Notification> findByType(String type) throws SQLException {
        String sql = "SELECT * FROM vw_notifications WHERE type = ? ORDER BY created_at DESC";
        return executeQuery(sql, type);
    }

    public List<Notification> findByReferenceId(int referenceId) throws SQLException {
        String sql = "SELECT * FROM vw_notifications WHERE reference_id = ? ORDER BY created_at DESC";
        return executeQuery(sql, referenceId);
    }

    @Override
    public boolean insert(Notification entity) throws SQLException {
        return executeProcedure("sp_send_notification",
                entity.getUserId(),
                entity.getMessage(),
                entity.getType(),
                entity.getReferenceId()
        );
    }

    public boolean markAsRead(int notificationId) throws SQLException {
        return executeProcedure("sp_mark_notification_read", notificationId);
    }

    public boolean markAllAsRead(int userId) throws SQLException {
        String sql = "UPDATE notifications SET is_read = true WHERE user_id = ? AND is_read = false";
        int result = executeUpdate(sql, userId);
        return result > 0;
    }

    @Override
    public boolean update(Notification entity) throws SQLException {
        String sql = "UPDATE notifications SET is_read = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.isRead(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM notifications WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public boolean deleteByUserId(int userId) throws SQLException {
        String sql = "DELETE FROM notifications WHERE user_id = ?";
        int result = executeUpdate(sql, userId);
        return result > 0;
    }

    public int countUnreadByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = false";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    public int countByUserId(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    protected Notification mapRow(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setUserName(rs.getString("user_name"));
        notification.setMessage(rs.getString("message"));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setType(rs.getString("type"));
        notification.setReferenceId(rs.getInt("reference_id"));

        if (rs.getTimestamp("created_at") != null) {
            notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            notification.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return notification;
    }
}