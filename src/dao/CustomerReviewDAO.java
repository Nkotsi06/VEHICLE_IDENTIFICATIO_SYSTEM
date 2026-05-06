package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.ProcedureCaller;
import database.ViewLoader;
import models.CustomerReview;

/**
 * CustomerReviewDAO - Uses ONLY stored procedures and views for all operations.
 *
 * @author Vehicle Identification System Team
 * @version 2.0
 */
public class CustomerReviewDAO extends BaseDAO<CustomerReview> {

    private final ProcedureCaller procedureCaller;
    private final ViewLoader viewLoader;

    public CustomerReviewDAO() {
        this.procedureCaller = new ProcedureCaller();
        this.viewLoader = new ViewLoader();
    }

    @Override
    public CustomerReview findById(int id) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_reviews", "id = ?", id);
        return results.isEmpty() ? null : mapToCustomerReview(results.get(0));
    }

    @Override
    public List<CustomerReview> findAll() throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadView("vw_customer_reviews");
        return mapToCustomerReviewList(results);
    }

    public List<CustomerReview> findByCustomerId(int customerId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_reviews",
                "customer_id = ? ORDER BY review_date DESC", customerId);
        return mapToCustomerReviewList(results);
    }

    public List<CustomerReview> findByWorkshopId(int workshopId) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_reviews",
                "workshop_id = ? ORDER BY review_date DESC", workshopId);
        return mapToCustomerReviewList(results);
    }

    public List<CustomerReview> findByRating(int rating) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_reviews",
                "rating = ? ORDER BY review_date DESC", rating);
        return mapToCustomerReviewList(results);
    }

    public List<CustomerReview> findHighRatedReviews(int minRating) throws SQLException {
        List<Map<String, Object>> results = viewLoader.loadViewWithCondition("vw_customer_reviews",
                "rating >= ? ORDER BY rating DESC, review_date DESC", minRating);
        return mapToCustomerReviewList(results);
    }

    /**
     * Counts the number of reviews submitted by a specific customer.
     */
    public int countByCustomerId(int customerId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_customer_reviews", "customer_id = ?", customerId);
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

    @Override
    public boolean update(CustomerReview entity) throws SQLException {
        // Reviews cannot be updated - only soft delete
        return false;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        return procedureCaller.executeDeleteReview(id);
    }

    public double getAverageRatingForWorkshop(int workshopId) throws SQLException {
        List<CustomerReview> reviews = findByWorkshopId(workshopId);
        if (reviews.isEmpty()) return 0.0;
        double sum = 0;
        for (CustomerReview review : reviews) {
            sum += review.getRating();
        }
        return sum / reviews.size();
    }

    public int getReviewCountForWorkshop(int workshopId) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_customer_reviews", "workshop_id = ?", workshopId);
    }

    public int getRatingDistribution(int workshopId, int rating) throws SQLException {
        return viewLoader.countViewRowsWithCondition("vw_customer_reviews", "workshop_id = ? AND rating = ?", workshopId, rating);
    }

    // ============================================
    // HELPER METHODS FOR MAPPING
    // ============================================

    private CustomerReview mapToCustomerReview(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        CustomerReview review = new CustomerReview();

        if (map.get("id") != null) review.setId(((Number) map.get("id")).intValue());
        if (map.get("customer_id") != null) review.setCustomerId(((Number) map.get("customer_id")).intValue());
        if (map.get("customer_name") != null) review.setCustomerName(map.get("customer_name").toString());
        if (map.get("workshop_id") != null) review.setWorkshopId(((Number) map.get("workshop_id")).intValue());
        if (map.get("workshop_name") != null) review.setWorkshopName(map.get("workshop_name").toString());
        if (map.get("rating") != null) review.setRating(((Number) map.get("rating")).intValue());
        if (map.get("review_text") != null) review.setReviewText(map.get("review_text").toString());

        if (map.get("review_date") != null) {
            Object reviewDateObj = map.get("review_date");
            if (reviewDateObj instanceof java.sql.Timestamp) {
                review.setReviewDate(((java.sql.Timestamp) reviewDateObj).toLocalDateTime());
            } else if (reviewDateObj instanceof LocalDateTime) {
                review.setReviewDate((LocalDateTime) reviewDateObj);
            }
        }
        if (map.get("created_at") instanceof java.sql.Timestamp) {
            review.setCreatedAt(((java.sql.Timestamp) map.get("created_at")).toLocalDateTime());
        }
        if (map.get("updated_at") instanceof java.sql.Timestamp) {
            review.setUpdatedAt(((java.sql.Timestamp) map.get("updated_at")).toLocalDateTime());
        }

        return review;
    }

    private List<CustomerReview> mapToCustomerReviewList(List<Map<String, Object>> maps) {
        List<CustomerReview> reviews = new ArrayList<>();
        if (maps != null) {
            for (Map<String, Object> map : maps) {
                reviews.add(mapToCustomerReview(map));
            }
        }
        return reviews;
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