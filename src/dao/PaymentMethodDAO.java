package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import database.ProcedureCaller;
import database.ViewLoader;
import models.PaymentMethod;

/**
 * PaymentMethodDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class PaymentMethodDAO extends BaseDAO<PaymentMethod> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public PaymentMethodDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public PaymentMethod findById(int id) throws SQLException {
        List<PaymentMethod> results = viewLoader.loadViewWithCondition("vw_payment_methods", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<PaymentMethod> findAll() throws SQLException {
        return viewLoader.loadView("vw_payment_methods");
    }

    public List<PaymentMethod> findByWalletId(int walletId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_payment_methods", "wallet_id = ? ORDER BY is_default DESC", walletId);
    }

    public List<PaymentMethod> findByCustomerId(int customerId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_payment_methods", "customer_id = ? ORDER BY is_default DESC", customerId);
    }

    public PaymentMethod findDefaultByWalletId(int walletId) throws SQLException {
        List<PaymentMethod> results = viewLoader.loadViewWithCondition("vw_payment_methods", "wallet_id = ? AND is_default = true", walletId);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public boolean insert(PaymentMethod entity) throws SQLException {
        Integer methodId = procedureCaller.executeInsertPaymentMethod(
                entity.getWalletId(),
                entity.getCardLastFour(),
                entity.getCardType(),
                entity.getExpiryMonth(),
                entity.getExpiryYear(),
                entity.isDefault()
        );
        if (methodId != null && methodId > 0) {
            entity.setId(methodId);
            return true;
        }
        return false;
    }

    public int insertAndGetId(PaymentMethod entity) throws SQLException {
        Integer methodId = procedureCaller.executeInsertPaymentMethod(
                entity.getWalletId(),
                entity.getCardLastFour(),
                entity.getCardType(),
                entity.getExpiryMonth(),
                entity.getExpiryYear(),
                entity.isDefault()
        );
        if (methodId != null && methodId > 0) {
            entity.setId(methodId);
            return methodId;
        }
        return -1;
    }

    public boolean setAsDefault(int paymentMethodId, int walletId) throws SQLException {
        // This operation needs to be transactional - clear others first, then set this one
        return procedureCaller.executeSetDefaultPaymentMethod(paymentMethodId, walletId);
    }

    @Override
    public boolean update(PaymentMethod entity) throws SQLException {
        return procedureCaller.executeUpdatePaymentMethod(
                entity.getId(),
                entity.getCardLastFour(),
                entity.getCardType(),
                entity.getExpiryMonth(),
                entity.getExpiryYear(),
                entity.isDefault()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeletePaymentMethod(id);
    }

    public boolean deleteByWalletId(int walletId) throws SQLException {
        return procedureCaller.executeDeletePaymentMethodsByWallet(walletId);
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