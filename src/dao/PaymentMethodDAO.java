package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.PaymentMethod;

public class PaymentMethodDAO extends BaseDAO<PaymentMethod> {

    @Override
    public PaymentMethod findById(int id) throws SQLException {
        String sql = "SELECT * FROM payment_methods WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<PaymentMethod> findAll() throws SQLException {
        String sql = "SELECT * FROM payment_methods ORDER BY wallet_id, is_default DESC";
        return executeQuery(sql);
    }

    public List<PaymentMethod> findByWalletId(int walletId) throws SQLException {
        String sql = "SELECT * FROM payment_methods WHERE wallet_id = ? ORDER BY is_default DESC, created_at DESC";
        return executeQuery(sql, walletId);
    }

    public List<PaymentMethod> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT pm.* FROM payment_methods pm " +
                "JOIN digital_wallets dw ON pm.wallet_id = dw.id " +
                "WHERE dw.customer_id = ? ORDER BY pm.is_default DESC";
        return executeQuery(sql, customerId);
    }

    public PaymentMethod findDefaultByWalletId(int walletId) throws SQLException {
        String sql = "SELECT * FROM payment_methods WHERE wallet_id = ? AND is_default = true";
        return executeQuerySingle(sql, walletId);
    }

    @Override
    public boolean insert(PaymentMethod entity) throws SQLException {
        String sql = "INSERT INTO payment_methods (wallet_id, card_last_four, card_type, expiry_month, expiry_year, is_default) VALUES (?, ?, ?, ?, ?, ?)";
        int result = executeUpdate(sql,
                entity.getWalletId(),
                entity.getCardLastFour(),
                entity.getCardType(),
                entity.getExpiryMonth(),
                entity.getExpiryYear(),
                entity.isDefault()
        );
        return result > 0;
    }

    public int insertAndGetId(PaymentMethod entity) throws SQLException {
        String sql = "INSERT INTO payment_methods (wallet_id, card_last_four, card_type, expiry_month, expiry_year, is_default) VALUES (?, ?, ?, ?, ?, ?)";
        return executeUpdateWithGeneratedKeys(sql,
                entity.getWalletId(),
                entity.getCardLastFour(),
                entity.getCardType(),
                entity.getExpiryMonth(),
                entity.getExpiryYear(),
                entity.isDefault()
        );
    }

    public boolean setAsDefault(int paymentMethodId, int walletId) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String clearDefaultSql = "UPDATE payment_methods SET is_default = false WHERE wallet_id = ?";
            ps = conn.prepareStatement(clearDefaultSql);
            ps.setInt(1, walletId);
            ps.executeUpdate();
            ps.close();

            String setDefaultSql = "UPDATE payment_methods SET is_default = true WHERE id = ?";
            ps = conn.prepareStatement(setDefaultSql);
            ps.setInt(1, paymentMethodId);
            ps.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (ps != null) ps.close();
            if (conn != null) conn.close();
        }
    }

    @Override
    public boolean update(PaymentMethod entity) throws SQLException {
        String sql = "UPDATE payment_methods SET card_last_four = ?, card_type = ?, expiry_month = ?, expiry_year = ?, is_default = ? WHERE id = ?";
        int result = executeUpdate(sql,
                entity.getCardLastFour(),
                entity.getCardType(),
                entity.getExpiryMonth(),
                entity.getExpiryYear(),
                entity.isDefault(),
                entity.getId()
        );
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM payment_methods WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public boolean deleteByWalletId(int walletId) throws SQLException {
        String sql = "DELETE FROM payment_methods WHERE wallet_id = ?";
        int result = executeUpdate(sql, walletId);
        return result > 0;
    }

    @Override
    protected PaymentMethod mapRow(ResultSet rs) throws SQLException {
        PaymentMethod method = new PaymentMethod();
        method.setId(rs.getInt("id"));
        method.setWalletId(rs.getInt("wallet_id"));
        method.setCardLastFour(rs.getString("card_last_four"));
        method.setCardType(rs.getString("card_type"));
        method.setExpiryMonth(rs.getInt("expiry_month"));
        method.setExpiryYear(rs.getInt("expiry_year"));
        method.setDefault(rs.getBoolean("is_default"));

        if (rs.getTimestamp("created_at") != null) {
            method.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            method.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return method;
    }
}