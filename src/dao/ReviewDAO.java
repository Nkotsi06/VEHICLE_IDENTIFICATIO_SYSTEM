package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.CustomerReview;

/**
 * ReviewDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class ReviewDAO extends BaseDAO<CustomerReview> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public ReviewDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public CustomerReview findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_reviews", "id = ?", id);
        if (results.isEmpty()) {
            return null;
        }
        return mapMapToCustomerReview(results.get(0));
    }

    @Override
    public List<CustomerReview> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_customer_reviews");
        return mapMapsToCustomerReviews(results);
    }

    public List<CustomerReview> findByCustomerId(int customerId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_reviews", "customer_id = ? ORDER BY review_date DESC", customerId);
        return mapMapsToCustomerReviews(results);
    }

    public List<CustomerReview> findByCustomerName(String customerName) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_reviews", "customer_name ILIKE ? ORDER BY review_date DESC", "%" + customerName + "%");
        return mapMapsToCustomerReviews(results);
    }

    public List<CustomerReview> findByWorkshopId(int workshopId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_reviews", "workshop_id = ? ORDER BY review_date DESC", workshopId);
        return mapMapsToCustomerReviews(results);
    }

    public List<CustomerReview> findByWorkshopName(String workshopName) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_reviews", "workshop_name ILIKE ? ORDER BY review_date DESC", "%" + workshopName + "%");
        return mapMapsToCustomerReviews(results);
    }

    public List<CustomerReview> findByRating(int rating) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_reviews", "rating = ? ORDER BY review_date DESC", rating);
        return mapMapsToCustomerReviews(results);
    }

    public List<CustomerReview> findHighRatedReviews(int minRating) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_reviews", "rating >= ? ORDER BY rating DESC, review_date DESC", minRating);
        return mapMapsToCustomerReviews(results);
    }

    public List<CustomerReview> findLowRatedReviews(int maxRating) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_reviews", "rating <= ? ORDER BY rating ASC, review_date DESC", maxRating);
        return mapMapsToCustomerReviews(results);
    }

    public List<CustomerReview> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_reviews", "review_date BETWEEN ? AND ? ORDER BY review_date DESC", startDate, endDate);
        return mapMapsToCustomerReviews(results);
    }

    @Override
    public boolean insert(CustomerReview entity) throws SQLException {
        Integer reviewId = procedureCaller.executeSubmitReview(
                entity.getCustomerId(),
                entity.getWorkshopId(),
                entity.getRating(),
                entity.getReviewText()
        );
        if (reviewId != null && reviewId > 0) {
            entity.setId(reviewId);
            return true;
        }
        return false;
    }

    public int insertAndGetId(CustomerReview entity) throws SQLException {
        return procedureCaller.executeSubmitReview(
                entity.getCustomerId(),
                entity.getWorkshopId(),
                entity.getRating(),
                entity.getReviewText()
        );
    }

    @Override
    public boolean update(CustomerReview entity) throws SQLException {
        return procedureCaller.executeUpdateReview(
                entity.getId(),
                entity.getRating(),
                entity.getReviewText()
        );
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteReview(id);
    }

    public boolean deleteByWorkshopId(int workshopId) throws SQLException {
        return procedureCaller.executeDeleteReviewsByWorkshop(workshopId);
    }

    public double getAverageRatingByWorkshop(int workshopId) throws SQLException {
        List<CustomerReview> reviews = findByWorkshopId(workshopId);
        if (reviews.isEmpty()) return 0.0;
        double sum = 0;
        for (CustomerReview review : reviews) {
            sum += review.getRating();
        }
        return sum / reviews.size();
    }

    public List<WorkshopRatingSummary> getWorkshopRatingSummaries() throws SQLException {
        List<WorkshopRatingSummary> summaries = new ArrayList<>();
        // Note: loadViewWithGroupBy may not exist in ViewLoader - need alternative
        List<Map<String, Object>> results = viewLoader.loadView("vw_customer_reviews");

        // Group manually
        Map<Integer, WorkshopRatingSummary> summaryMap = new HashMap<>();
        for (Map<String, Object> row : results) {
            Integer workshopId = getIntValue(row, "workshop_id");
            String workshopName = getStringValue(row, "workshop_name");
            Integer rating = getIntValue(row, "rating");

            WorkshopRatingSummary summary = summaryMap.get(workshopId);
            if (summary == null) {
                summary = new WorkshopRatingSummary();
                summary.workshopId = workshopId;
                summary.workshopName = workshopName;
                summary.reviewCount = 0;
                summary.totalRating = 0;
                summary.minRating = 5;
                summary.maxRating = 0;
                summaryMap.put(workshopId, summary);
            }
            summary.reviewCount++;
            summary.totalRating += rating;
            summary.minRating = Math.min(summary.minRating, rating);
            summary.maxRating = Math.max(summary.maxRating, rating);
        }

        for (WorkshopRatingSummary summary : summaryMap.values()) {
            summary.averageRating = summary.totalRating / (double) summary.reviewCount;
            summaries.add(summary);
        }

        summaries.sort((a, b) -> Double.compare(b.averageRating, a.averageRating));
        return summaries;
    }

    public static class WorkshopRatingSummary {
        public int workshopId;
        public String workshopName;
        public int reviewCount;
        public double averageRating;
        public int minRating;
        public int maxRating;
        public int totalRating;
    }

    /**
     * Converts a List of Maps to a List of CustomerReview objects.
     */
    private List<CustomerReview> mapMapsToCustomerReviews(List<Map<String, Object>> maps) {
        List<CustomerReview> reviews = new ArrayList<>();
        if (maps == null) {
            return reviews;
        }
        for (Map<String, Object> map : maps) {
            CustomerReview review = mapMapToCustomerReview(map);
            if (review != null) {
                reviews.add(review);
            }
        }
        return reviews;
    }

    /**
     * Converts a Map to a CustomerReview object.
     */
    private CustomerReview mapMapToCustomerReview(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        CustomerReview review = new CustomerReview();

        review.setId(getIntValue(map, "id"));
        review.setCustomerId(getIntValue(map, "customer_id"));
        review.setCustomerName(getStringValue(map, "customer_name"));
        review.setWorkshopId(getIntValue(map, "workshop_id"));
        review.setWorkshopName(getStringValue(map, "workshop_name"));
        review.setRating(getIntValue(map, "rating"));
        review.setReviewText(getStringValue(map, "review_text"));

        review.setReviewDate(getLocalDateTimeValue(map, "review_date"));
        review.setCreatedAt(getLocalDateTimeValue(map, "created_at"));
        review.setUpdatedAt(getLocalDateTimeValue(map, "updated_at"));

        return review;
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
    protected CustomerReview mapRow(ResultSet rs) throws SQLException {
        CustomerReview review = new CustomerReview();
        review.setId(rs.getInt("id"));
        review.setCustomerId(rs.getInt("customer_id"));
        review.setCustomerName(rs.getString("customer_name"));
        review.setWorkshopId(rs.getInt("workshop_id"));
        review.setWorkshopName(rs.getString("workshop_name"));
        review.setRating(rs.getInt("rating"));
        review.setReviewText(rs.getString("review_text"));

        if (rs.getTimestamp("review_date") != null) {
            review.setReviewDate(rs.getTimestamp("review_date").toLocalDateTime());
        }
        if (rs.getTimestamp("created_at") != null) {
            review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            review.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return review;
    }
}