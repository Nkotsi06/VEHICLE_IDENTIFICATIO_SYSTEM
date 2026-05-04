package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        List<CustomerReview> results = viewLoader.loadViewWithCondition("vw_customer_reviews", "id = ?", id);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<CustomerReview> findAll() throws SQLException {
        return viewLoader.loadView("vw_customer_reviews");
    }

    public List<CustomerReview> findByCustomerId(int customerId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_customer_reviews", "customer_id = ? ORDER BY review_date DESC", customerId);
    }

    public List<CustomerReview> findByCustomerName(String customerName) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_customer_reviews", "customer_name ILIKE ? ORDER BY review_date DESC", "%" + customerName + "%");
    }

    public List<CustomerReview> findByWorkshopId(int workshopId) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_customer_reviews", "workshop_id = ? ORDER BY review_date DESC", workshopId);
    }

    public List<CustomerReview> findByWorkshopName(String workshopName) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_customer_reviews", "workshop_name ILIKE ? ORDER BY review_date DESC", "%" + workshopName + "%");
    }

    public List<CustomerReview> findByRating(int rating) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_customer_reviews", "rating = ? ORDER BY review_date DESC", rating);
    }

    public List<CustomerReview> findHighRatedReviews(int minRating) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_customer_reviews", "rating >= ? ORDER BY rating DESC, review_date DESC", minRating);
    }

    public List<CustomerReview> findLowRatedReviews(int maxRating) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_customer_reviews", "rating <= ? ORDER BY rating ASC, review_date DESC", maxRating);
    }

    public List<CustomerReview> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        return viewLoader.loadViewWithCondition("vw_customer_reviews", "review_date BETWEEN ? AND ? ORDER BY review_date DESC", startDate, endDate);
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
        List<Map<String, Object>> results = viewLoader.loadViewWithGroupBy(
                "vw_customer_reviews",
                "workshop_id, workshop_name, COUNT(*) as review_count, AVG(rating) as avg_rating, " +
                        "MIN(rating) as min_rating, MAX(rating) as max_rating",
                "workshop_id, workshop_name ORDER BY avg_rating DESC"
        );

        for (Map<String, Object> row : results) {
            WorkshopRatingSummary summary = new WorkshopRatingSummary();
            summary.workshopId = (Integer) row.get("workshop_id");
            summary.workshopName = (String) row.get("workshop_name");
            summary.reviewCount = ((Number) row.get("review_count")).intValue();
            summary.averageRating = ((Number) row.get("avg_rating")).doubleValue();
            summary.minRating = ((Number) row.get("min_rating")).intValue();
            summary.maxRating = ((Number) row.get("max_rating")).intValue();
            summaries.add(summary);
        }
        return summaries;
    }

    public static class WorkshopRatingSummary {
        public int workshopId;
        public String workshopName;
        public int reviewCount;
        public double averageRating;
        public int minRating;
        public int maxRating;
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