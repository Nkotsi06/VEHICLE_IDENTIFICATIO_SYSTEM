package dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import database.DatabaseConnection;

public abstract class BaseDAO<T> {

    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    protected void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try { if (rs != null) rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (stmt != null) stmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        try { if (conn != null && conn != DatabaseConnection.getInstance().getConnection()) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }

    protected void closeResources(Statement stmt, Connection conn) {
        closeResources(null, stmt, conn);
    }

    // ============================================
    // AUDIT LOGGING METHODS
    // ============================================

    /**
     * Log an audit action to the database
     * @param userId The ID of the user performing the action
     * @param action The action being performed
     * @param ipAddress The IP address of the user
     */
    protected void logAudit(int userId, String action, String ipAddress) {
        try {
            executeProcedure("sp_log_audit_action", userId, action, ipAddress);
        } catch (SQLException e) {
            System.err.println("Failed to log audit: " + e.getMessage());
        }
    }

    /**
     * Log an audit action with default IP address
     * @param userId The ID of the user performing the action
     * @param action The action being performed
     */
    protected void logAudit(int userId, String action) {
        logAudit(userId, action, "127.0.0.1");
    }

    /**
     * Get current user ID from session (to be used in DAO methods)
     */
    protected int getCurrentUserId() {
        try {
            Integer userId = utils.SessionManager.getInstance().getUserId();
            return userId != null ? userId : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    // ============================================
    // READ OPERATIONS (USING VIEWS)
    // ============================================

    protected int executeUpdate(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            setParameters(ps, params);
            return ps.executeUpdate();
        } finally {
            closeResources(ps, conn);
        }
    }

    protected int executeUpdateWithGeneratedKeys(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            setParameters(ps, params);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    protected List<T> executeQuery(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<T> results = new ArrayList<>();
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            setParameters(ps, params);
            rs = ps.executeQuery();
            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    protected T executeQuerySingle(String sql, Object... params) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            setParameters(ps, params);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    // ============================================
    // POSTGRESQL PROCEDURE CALL METHODS
    // ============================================

    private String buildPostgresCall(String procedureName, int paramCount) {
        StringBuilder sql = new StringBuilder("CALL ");
        sql.append(procedureName).append("(");
        for (int i = 0; i < paramCount; i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");
        return sql.toString();
    }

    protected boolean executeProcedure(String procedureName, Object... params) throws SQLException {
        String sql = buildPostgresCall(procedureName, params.length);
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall(sql);
            setProcedureParameters(cs, params);
            return cs.execute();
        } finally {
            closeResources(cs, conn);
        }
    }

    @SuppressWarnings("unchecked")
    protected <R> R executeProcedureWithOutParameter(String procedureName, int outParamType, Object... params) throws SQLException {
        String sql = buildPostgresCall(procedureName, params.length + 1);
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall(sql);
            for (int i = 0; i < params.length; i++) {
                if (params[i] == null) {
                    cs.setNull(i + 1, Types.NULL);
                } else {
                    cs.setObject(i + 1, params[i]);
                }
            }
            cs.registerOutParameter(params.length + 1, outParamType);
            cs.execute();
            return (R) cs.getObject(params.length + 1);
        } finally {
            closeResources(cs, conn);
        }
    }

    protected Integer executeProcedureWithInOutParameter(String procedureName, Object... params) throws SQLException {
        return executeProcedureWithOutParameter(procedureName, Types.INTEGER, params);
    }

    protected int executeProcedureUpdate(String procedureName, Object... params) throws SQLException {
        String sql = buildPostgresCall(procedureName, params.length);
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = getConnection();
            cs = conn.prepareCall(sql);
            setProcedureParameters(cs, params);
            cs.execute();
            return cs.getUpdateCount();
        } finally {
            closeResources(cs, conn);
        }
    }

    private void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                ps.setNull(i + 1, Types.NULL);
            } else {
                ps.setObject(i + 1, params[i]);
            }
        }
    }

    private void setProcedureParameters(CallableStatement cs, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            if (params[i] == null) {
                cs.setNull(i + 1, Types.NULL);
            } else {
                cs.setObject(i + 1, params[i]);
            }
        }
    }

    // ============================================
    // ABSTRACT METHODS
    // ============================================

    protected abstract T mapRow(ResultSet rs) throws SQLException;
    public abstract T findById(int id) throws SQLException;
    public abstract List<T> findAll() throws SQLException;
    public abstract boolean insert(T entity) throws SQLException;
    public abstract boolean update(T entity) throws SQLException;
    public abstract boolean delete(int id) throws SQLException;
}