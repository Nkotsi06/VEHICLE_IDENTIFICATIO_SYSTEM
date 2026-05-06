package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_payment_methods", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPaymentMethod(results.get(0));
    }

    @Override
    public List<PaymentMethod> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_payment_methods");
        return mapMapsToPaymentMethods(results);
    }

    public List<PaymentMethod> findByWalletId(int walletId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_payment_methods", "wallet_id = ? ORDER BY is_default DESC", walletId);
        return mapMapsToPaymentMethods(results);
    }

    public List<PaymentMethod> findByCustomerId(int customerId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_payment_methods", "customer_id = ? ORDER BY is_default DESC", customerId);
        return mapMapsToPaymentMethods(results);
    }

    public PaymentMethod findDefaultByWalletId(int walletId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_payment_methods", "wallet_id = ? AND is_default = true", walletId);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToPaymentMethod(results.get(0));
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

    /**
     * Converts a List of Maps to a List of PaymentMethod objects.
     */
    private List<PaymentMethod> mapMapsToPaymentMethods(List<Map<String, Object>> maps) {
        List<PaymentMethod> methods = new ArrayList<>();
        if (maps == null) {
            return methods;
        }
        for (Map<String, Object> map : maps) {
            PaymentMethod method = mapMapToPaymentMethod(map);
            if (method != null) {
                methods.add(method);
            }
        }
        return methods;
    }

    /**
     * Converts a Map to a PaymentMethod object.
     */
    private PaymentMethod mapMapToPaymentMethod(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        PaymentMethod method = new PaymentMethod();

        method.setId(getIntValue(map, "id"));
        method.setWalletId(getIntValue(map, "wallet_id"));
        method.setCardLastFour(getStringValue(map, "card_last_four"));
        method.setCardType(getStringValue(map, "card_type"));
        method.setExpiryMonth(getIntValue(map, "expiry_month"));
        method.setExpiryYear(getIntValue(map, "expiry_year"));

        Boolean isDefault = (Boolean) map.get("is_default");
        method.setDefault(isDefault != null && isDefault);

        method.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        method.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return method;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Long) return ((Long) value).intValue();
        if (value instanceof Number) return ((Number) value).intValue();
        return 0;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        return null;
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