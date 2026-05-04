package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.Notification;

/**
 * NotificationDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class NotificationDAO extends BaseDAO<Notification> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public NotificationDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public Notification findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_notifications", "id = ?", id);
        return results.isEmpty() ? null : mapToNotification(results.get(0));
    }

    @Override
    public List<Notification> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_notifications");
        return mapToNotificationList(results);
    }

    public List<Notification> findByUserId(int userId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_notifications", "user_id = ? ORDER BY created_at DESC", userId);
        return mapToNotificationList(results);
    }

    public List<Notification> findUnreadByUserId(int userId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_notifications", "user_id = ? AND is_read = false ORDER BY created_at DESC", userId);
        return mapToNotificationList(results);
    }

    public List<Notification> findByType(String type) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_notifications", "type = ? ORDER BY created_at DESC", type);
        return mapToNotificationList(results);
    }

    public List<Notification> findByReferenceId(int referenceId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_notifications", "reference_id = ? ORDER BY created_at DESC", referenceId);
        return mapToNotificationList(results);
    }

    @Override
    public boolean insert(Notification entity) throws SQLException {
        return procedureCaller.executeSendNotification(
                entity.getUserId(),
                entity.getMessage(),
                entity.getType(),
                entity.getReferenceId()
        );
    }

    public boolean markAsRead(int notificationId) throws SQLException {
        return procedureCaller.executeMarkNotificationRead(notificationId);
    }

    public boolean markAllAsRead(int userId) throws SQLException {
        return procedureCaller.executeMarkAllNotificationsRead(userId);
    }

    @Override
    public boolean update(Notification entity) throws SQLException {
        if (entity.isRead()) {
            return markAsRead(entity.getId());
        }
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteNotification(id);
    }

    public boolean deleteByUserId(int userId) throws SQLException {
        return procedureCaller.executeDeleteNotificationsByUser(userId);
    }

    public int countUnreadByUserId(int userId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_notifications", "user_id = ? AND is_read = false", userId);
    }

    public int countByUserId(int userId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_notifications", "user_id = ?", userId);
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    private Notification mapToNotification(Map<String, Object> map) {
        if (map == null) return null;

        Notification notification = new Notification();
        if (map.get("id") != null) notification.setId(((Number) map.get("id")).intValue());
        if (map.get("user_id") != null) notification.setUserId(((Number) map.get("user_id")).intValue());
        if (map.get("user_name") != null) notification.setUserName(map.get("user_name").toString());
        if (map.get("message") != null) notification.setMessage(map.get("message").toString());
        if (map.get("is_read") != null) notification.setRead((Boolean) map.get("is_read"));
        if (map.get("type") != null) notification.setType(map.get("type").toString());
        if (map.get("reference_id") != null) notification.setReferenceId(((Number) map.get("reference_id")).intValue());

        if (map.get("created_at") instanceof java.sql.Timestamp) {
            notification.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof java.sql.Timestamp) {
            notification.setUpdatedAt(((java.sql.Timestamp) map.get("updated_at")).toLocalDateTime());
        }
        return notification;
    }

    private List<Notification> mapToNotificationList(List<Map<String, Object>> maps) {
        List<Notification> notifications = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                notifications.add(mapToNotification(map));
            }
        }
        return notifications;
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