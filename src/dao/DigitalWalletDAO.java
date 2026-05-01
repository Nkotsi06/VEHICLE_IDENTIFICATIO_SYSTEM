package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import models.DigitalWallet;

public class DigitalWalletDAO extends BaseDAO<DigitalWallet> {

    @Override
    public DigitalWallet findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_digital_wallet WHERE wallet_id = ?";
        return executeQuerySingle(sql, id);
    }

    public DigitalWallet findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM vw_digital_wallet WHERE customer_id = ?";
        return executeQuerySingle(sql, customerId);
    }

    public double getBalanceByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT COALESCE(balance, 0) FROM digital_wallets WHERE customer_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } finally {
            closeResources(rs, ps, conn);
        }
    }

    @Override
    public List<DigitalWallet> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_digital_wallet ORDER BY customer_name";
        return executeQuery(sql);
    }

    public List<DigitalWallet> findWalletsWithBalance(double minBalance) throws SQLException {
        String sql = "SELECT * FROM vw_digital_wallet WHERE balance >= ? ORDER BY balance DESC";
        return executeQuery(sql, minBalance);
    }

    @Override
    public boolean insert(DigitalWallet entity) throws SQLException {
        return executeProcedure("sp_create_digital_wallet", entity.getCustomerId());
    }

    public boolean addBalance(int customerId, double amount, String referenceId) throws SQLException {
        return executeProcedure("sp_add_wallet_balance", customerId, amount, referenceId);
    }

    public boolean deductBalance(int customerId, double amount, String referenceId) throws SQLException {
        return executeProcedure("sp_pay_from_wallet", customerId, amount, referenceId);
    }

    @Override
    public boolean update(DigitalWallet entity) throws SQLException {
        String sql = "UPDATE digital_wallets SET balance = ? WHERE customer_id = ?";
        int result = executeUpdate(sql, entity.getBalance(), entity.getCustomerId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM digital_wallets WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    @Override
    protected DigitalWallet mapRow(ResultSet rs) throws SQLException {
        DigitalWallet wallet = new DigitalWallet();
        wallet.setId(rs.getInt("wallet_id"));
        wallet.setCustomerId(rs.getInt("customer_id"));
        wallet.setCustomerName(rs.getString("customer_name"));
        wallet.setBalance(rs.getDouble("balance"));

        if (rs.getTimestamp("created_at") != null) {
            wallet.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            wallet.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return wallet;
    }
}