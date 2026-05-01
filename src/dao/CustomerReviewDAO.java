package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import models.CustomerReview;

public class CustomerReviewDAO extends BaseDAO<CustomerReview> {

    @Override
    public CustomerReview findById(int id) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE id = ?";
        return executeQuerySingle(sql, id);
    }

    @Override
    public List<CustomerReview> findAll() throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews ORDER BY review_date DESC";
        return executeQuery(sql);
    }

    public List<CustomerReview> findByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE customer_id = ? ORDER BY review_date DESC";
        return executeQuery(sql, customerId);
    }

    public List<CustomerReview> findByWorkshopId(int workshopId) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE workshop_id = ? ORDER BY review_date DESC";
        return executeQuery(sql, workshopId);
    }

    public List<CustomerReview> findByRating(int rating) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE rating = ? ORDER BY review_date DESC";
        return executeQuery(sql, rating);
    }

    public List<CustomerReview> findHighRatedReviews(int minRating) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE rating >= ? ORDER BY rating DESC, review_date DESC";
        return executeQuery(sql, minRating);
    }

    public List<CustomerReview> findLowRatedReviews(int maxRating) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE rating <= ? ORDER BY rating ASC, review_date DESC";
        return executeQuery(sql, maxRating);
    }

    public List<CustomerReview> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT * FROM vw_customer_reviews WHERE review_date BETWEEN ? AND ? ORDER BY review_date DESC";
        return executeQuery(sql, startDate, endDate);
    }

    public int countByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM customer_reviews WHERE customer_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
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
    public boolean insert(CustomerReview entity) throws SQLException {
        return executeProcedure("sp_submit_review",
                entity.getCustomerId(),
                entity.getWorkshopId(),
                entity.getRating(),
                entity.getReviewText()
        );
    }

    @Override
    public boolean update(CustomerReview entity) throws SQLException {
        String sql = "UPDATE customer_reviews SET rating = ?, review_text = ? WHERE id = ?";
        int result = executeUpdate(sql, entity.getRating(), entity.getReviewText(), entity.getId());
        return result > 0;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM customer_reviews WHERE id = ?";
        int result = executeUpdate(sql, id);
        return result > 0;
    }

    public double getAverageRatingByWorkshop(int workshopId) throws SQLException {
        String sql = "SELECT COALESCE(AVG(rating), 0) FROM customer_reviews WHERE workshop_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workshopId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0;
        } finally {
            closeResources(rs, ps, conn);
        }
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